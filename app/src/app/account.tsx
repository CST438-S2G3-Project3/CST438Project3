import { useEffect, useState } from 'react';
import { ActivityIndicator, Pressable, ScrollView, StyleSheet, TextInput } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { MaxContentWidth, Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';
import { supabase } from '@/lib/supabase';
import { Redirect } from 'expo-router';
import { useAuthSession } from '@/hooks/use-auth-session';

function getErrorMessage(error: unknown) {
    return error instanceof Error ? error.message : 'Something went wrong.';
}

export default function AccountScreen() {
    const theme = useTheme();

    const [userId, setUserId] = useState<string | null>(null);
    const [email, setEmail] = useState('');
    const [username, setUsername] = useState('');
    const [bio, setBio] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [loadingProfile, setLoadingProfile] = useState(true);
    const [saving, setSaving] = useState(false);
    const [message, setMessage] = useState('');

    const { isSignedIn, isAuthReady } = useAuthSession();

    useEffect(() => {
        let isActive = true;

        async function loadAccount() {
            try {
                const {
                    data: { user },
                    error: userError,
                } = await supabase.auth.getUser();

                if (userError) throw userError;

                if (!user) {
                    if (isActive) {
                        setMessage("Sign in with GitHub or Google to view your account.");
                    }
                    return;
                }

                if (isActive) {
                    setUserId(user.id);
                    setEmail(user.email ?? '');
                }

                const { data: profile, error: profileError } = await supabase.from('profiles').select('username, bio').eq('id', user.id).maybeSingle();

                if (profileError) throw profileError;

                if (isActive) {
                    setUsername(profile?.username ?? '');
                    setBio(profile?.bio ?? '');
                }
            } catch (error) {
                if (isActive) setMessage(getErrorMessage(error));
            } finally {
                if (isActive) setLoadingProfile(false);
            }
        }

        void loadAccount();

        return () => {
            isActive = false;
        };
    }, []);

    async function saveProfile() {
        if (!userId) {
            setMessage('You must be signed in to save your profile.');
            return;
        }

        const trimmedUsername = username.trim();
        const trimmedBio = bio.trim();

        if (trimmedUsername.length < 3 || trimmedUsername.length > 30) {
            setMessage('Username must be between 3 and 30 characters.');
            return;
        }

        if (trimmedBio.length > 500) {
            setMessage('Bio must be 500 characters or fewer.');
            return;
        }

        setSaving(true);
        setMessage('');

        try {
            const { error } = await supabase.from('profiles').upsert(
                {
                    id: userId,
                    username: trimmedUsername,
                    bio: trimmedBio,
                    updated_at: new Date().toISOString(),
                },
                { onConflict: 'id' }
            );

            if (error) throw error;

            setUsername(trimmedUsername);
            setBio(trimmedBio);
            setMessage('Profile saved.');
        } catch (error) {
            setMessage(getErrorMessage(error));
        } finally {
            setSaving(false);
        }
    }

    async function changePassword() {
        if (!newPassword) {
            setMessage('Enter a new password.');
            return;
        }

        if (newPassword.length < 8) {
            setMessage('Use a password with at least 8 characters.');
            return;
        }

        if (newPassword !== confirmPassword) {
            setMessage('The passwords do not match.');
            return;
        }

        setSaving(true);
        setMessage('');

        try {
            const { error } = await supabase.auth.updateUser({
                password: newPassword,
            });

            if (error) throw error;

            setNewPassword('');
            setConfirmPassword('');
            setMessage('Password updated.');
        } catch (error) {
            setMessage(getErrorMessage(error));
        } finally {
            setSaving(false);
        }
    }

    const inputStyle = [
        styles.input,
        {
            color: theme.text,
            backgroundColor: theme.background,
            borderColor: theme.textSecondary,
        },
    ];

    if (!isAuthReady || loadingProfile) {
        return (
            <ThemedView style={styles.loadingContainer}>
                <ActivityIndicator color={theme.text} />
                <ThemedText>Loading account...</ThemedText>
            </ThemedView>
        );
    }

    if (!isSignedIn) {
        return <Redirect href="/" />;
    }

    return (
        <SafeAreaView style={styles.safeArea}>
            <ScrollView contentContainerStyle={styles.content} keyboardShouldPersistTaps="handled">
                <ThemedText type="title">Account</ThemedText>

                {email ? (
                    <ThemedText themeColor='textSecondary'>{email}</ThemedText>
                ) : null}

                <ThemedView type="backgroundElement" style={styles.section}>
                    <ThemedText type='subtitle'>Profile</ThemedText>

                    <ThemedText type="smallBold">Username</ThemedText>
                    <TextInput value={username} onChangeText={setUsername} placeholder="Choose a username" placeholderTextColor={theme.textSecondary} autoCapitalize="none" autoCorrect={false} maxLength={30} style={inputStyle} />

                    <ThemedText type="smallBold">Biography</ThemedText>
                    <TextInput value={bio} onChangeText={setBio} placeholder="Tell people a little about yourself" placeholderTextColor={theme.textSecondary} multiline maxLength={500} textAlignVertical="top" style={[inputStyle, styles.bioInput]} />

                    <Pressable accessibilityRole="button" disabled={saving} onPress={saveProfile} style={({ pressed }) => [
                        styles.button,
                        { opacity: pressed || saving ? 0.7 : 1 },
                    ]} >
                        <ThemedView type="backgroundSelected" style={styles.buttonInner}>
                            <ThemedText type="smallBold">
                                {saving ? 'Saving...' : 'Save Profile'}
                            </ThemedText>
                        </ThemedView>
                    </Pressable>
                </ThemedView>

                <ThemedView type="backgroundElement" style={styles.section}>
                    <ThemedText type="subtitle">Change password</ThemedText>

                    <ThemedText type="smallBold">New password</ThemedText>
                    <TextInput value={newPassword} onChangeText={setNewPassword} placeholder="At least 8 characters" placeholderTextColor={theme.textSecondary} secureTextEntry autoCapitalize="none" autoCorrect={false} style={inputStyle} />

                    <ThemedText type="smallBold">Confirm new password</ThemedText>
                    <TextInput value={confirmPassword} onChangeText={setConfirmPassword} placeholder="Re-enter your new password" placeholderTextColor={theme.textSecondary} secureTextEntry autoCapitalize="none" autoCorrect={false} style={inputStyle} />

                    <Pressable accessibilityRole="button" disabled={saving} onPress={changePassword} style={({ pressed }) => [
                        styles.button,
                        { opacity: pressed || saving ? 0.7 : 1 },
                    ]} >
                        <ThemedView type="backgroundSelected" style={styles.buttonInner}>
                            <ThemedText type="smallBold">
                                {saving ? 'Saving...' : 'Update password'}
                            </ThemedText>
                        </ThemedView>
                    </Pressable>
                </ThemedView>

                {message ? (
                    <ThemedText accessibilityLiveRegion="polite">{message}</ThemedText>
                ) : null}
            </ScrollView>
        </SafeAreaView>
    );
}

const styles = StyleSheet.create({
    safeArea: {
        flex: 1,
    },
    loadingContainer: {
        flex: 1,
        alignItems: 'center',
        gap: Spacing.three,
    },
    content: {
        flexGrow: 1,
        width: '100%',
        maxWidth: MaxContentWidth,
        alignSelf: 'center',
        padding: Spacing.four,
        paddingBottom: Spacing.six,
        gap: Spacing.three,
    },
    section: {
        padding: Spacing.four,
        borderRadius: Spacing.three,
        gap: Spacing.two,
    },
    input: {
        minHeight: 48,
        borderWidth: 1,
        borderRadius: Spacing.two,
        paddingHorizontal: Spacing.three,
        paddingVertical: Spacing.two,
    },
    bioInput: {
        minHeight: 120,
    },
    button: {
        marginTop: Spacing.two,
        borderRadius: Spacing.two,
        overflow: 'hidden',
    },
    buttonInner: {
        minHeight: 48,
        alignItems: 'center',
        justifyContent: 'center',
        paddingHorizontal: Spacing.three,
    }
});