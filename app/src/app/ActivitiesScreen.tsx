import React, { useState, useEffect } from 'react';
import {
    View,
    Text,
    FlatList,
    TextInput,
    TouchableOpacity,
    StyleSheet,
    ActivityIndicator,
    Alert,      // Added for mobile confirmation popups
    Platform,   // Added to detect if we are on web vs. mobile
} from 'react-native';

// 1. Describe the shape of an Activity coming from your Spring Boot API
interface Activity {
    id: string;
    destinationId: string;
    title: string;
    scheduledAt: string;
    createdAt: string;
}

// You can easily adjust this URL if your backend port changes
const API_URL = 'http://localhost:8080/api/v1/activities';

export default function ActivitiesScreen() {
    const [activities, setActivities] = useState<Activity[]>([]);
    const [loading, setLoading] = useState(true);
    const [newTitle, setNewTitle] = useState('');
    const [submitting, setSubmitting] = useState(false);
    const [deletingId, setDeletingId] = useState<string | null>(null);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);

    // Run fetchActivities once when the screen first loads
    useEffect(() => {
        fetchActivities();
    }, []);

    // GET route: Fetch all activities to show on the screen
    const fetchActivities = async () => {
        try {
            setErrorMessage(null);
            const response = await fetch(API_URL);

            if (!response.ok) {
                throw new Error(`GET failed with status: ${response.status}`);
            }

            const data: Activity[] = await response.json();
            setActivities(data);
        } catch (error) {
            console.error('Error fetching activities:', error);
            setErrorMessage('Could not load activities. Is the API running?');
        } finally {
            setLoading(false);
        }
    };

    // POST route: Create a new activity and refresh the list
    const handleAddActivity = async () => {
        if (!newTitle.trim()) return;

        try {
            setSubmitting(true);
            setErrorMessage(null);

            const payload = {
                destinationId: 'c817132d-4305-4c7e-b270-fb24bc6661a2', // Paris destination UUID
                title: newTitle.trim(),
                scheduledAt: new Date().toISOString(),
            };

            const response = await fetch(API_URL, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(payload),
            });

            if (!response.ok) {
                throw new Error(`POST failed with status: ${response.status}`);
            }

            setNewTitle('');
            await fetchActivities();
        } catch (error) {
            console.error('Error creating activity:', error);
            setErrorMessage('Could not create activity. Check the browser console for details.');
        } finally {
            setSubmitting(false);
        }
    };

    // NEW: Ask the user for confirmation before calling the DELETE route
    const confirmDeleteActivity = (activity: Activity) => {
        const message = `Are you sure you want to delete "${activity.title}"?`;

        if (Platform.OS === 'web') {
            // Browser confirmation popup
            const confirmed = window.confirm(message);
            if (confirmed) {
                handleDeleteActivity(activity.id);
            }
        } else {
            // Native iOS / Android confirmation popup
            Alert.alert('Delete Activity', message, [
                { text: 'Cancel', style: 'cancel' },
                {
                    text: 'Delete',
                    style: 'destructive',
                    onPress: () => handleDeleteActivity(activity.id),
                },
            ]);
        }
    };

    // DELETE route: Remove an activity by its ID
    const handleDeleteActivity = async (id: string) => {
        try {
            setDeletingId(id);
            setErrorMessage(null);

            const response = await fetch(`${API_URL}/${id}`, {
                method: 'DELETE',
            });

            if (!response.ok) {
                throw new Error(`DELETE failed with status: ${response.status}`);
            }

            // Immediately remove the deleted activity from our local list
            setActivities((prevActivities) =>
                prevActivities.filter((activity) => activity.id !== id)
            );
        } catch (error) {
            console.error('Error deleting activity:', error);
            setErrorMessage('Could not delete activity. Check the browser console for details.');
        } finally {
            setDeletingId(null);
        }
    };

    return (
        <View style={styles.container}>
            <Text style={styles.header}>Trip Activities</Text>

            {/* Form to test the POST route */}
            <View style={styles.form}>
                <TextInput
                    style={styles.input}
                    placeholder="Enter a new activity (e.g., Beach Walk)"
                    value={newTitle}
                    onChangeText={setNewTitle}
                />
                <TouchableOpacity
                    style={[styles.button, submitting && styles.buttonDisabled]}
                    onPress={handleAddActivity}
                    disabled={submitting}
                >
                    <Text style={styles.buttonText}>
                        {submitting ? 'Adding...' : 'Add'}
                    </Text>
                </TouchableOpacity>
            </View>

            {/* Show an error banner if something goes wrong */}
            {errorMessage && <Text style={styles.errorText}>{errorMessage}</Text>}

            {/* List to show activities from the GET route */}
            {loading ? (
                <View style={styles.centered}>
                    <ActivityIndicator size="large" color="#208AEF" />
                </View>
            ) : (
                <FlatList
                    data={activities}
                    keyExtractor={(item) => item.id}
                    renderItem={({ item }) => (
                        <View style={styles.card}>
                            {/* Left side: Activity title and scheduled date */}
                            <View style={styles.cardContent}>
                                <Text style={styles.title}>{item.title}</Text>
                                <Text style={styles.date}>
                                    Scheduled: {new Date(item.scheduledAt).toLocaleString()}
                                </Text>
                            </View>

                            {/* Right side: Delete button now triggers the confirmation popup */}
                            <TouchableOpacity
                                style={[
                                    styles.deleteButton,
                                    deletingId === item.id && styles.buttonDisabled,
                                ]}
                                onPress={() => confirmDeleteActivity(item)}
                                disabled={deletingId === item.id}
                            >
                                <Text style={styles.deleteButtonText}>
                                    {deletingId === item.id ? '...' : 'Delete'}
                                </Text>
                            </TouchableOpacity>
                        </View>
                    )}
                    ListEmptyComponent={
                        <Text style={styles.empty}>No activities yet. Add one above!</Text>
                    }
                />
            )}
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        padding: 20,
        backgroundColor: '#f5f5f5',
        maxWidth: 600,
        width: '100%',
        alignSelf: 'center',
    },
    centered: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
    },
    header: {
        fontSize: 28,
        fontWeight: 'bold',
        marginBottom: 16,
        marginTop: 20,
        color: '#222',
    },
    form: {
        flexDirection: 'row',
        marginBottom: 16,
        gap: 8,
    },
    input: {
        flex: 1,
        backgroundColor: '#fff',
        paddingHorizontal: 12,
        paddingVertical: 10,
        borderRadius: 8,
        borderWidth: 1,
        borderColor: '#ddd',
        fontSize: 16,
    },
    button: {
        backgroundColor: '#208AEF',
        paddingHorizontal: 20,
        justifyContent: 'center',
        borderRadius: 8,
    },
    buttonDisabled: {
        opacity: 0.6,
    },
    buttonText: {
        color: '#fff',
        fontWeight: 'bold',
        fontSize: 16,
    },
    errorText: {
        color: '#d93025',
        marginBottom: 12,
        fontSize: 14,
    },
    card: {
        backgroundColor: '#fff',
        padding: 16,
        borderRadius: 8,
        marginBottom: 12,
        borderWidth: 1,
        borderColor: '#eee',
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
        gap: 12,
    },
    cardContent: {
        flex: 1,
    },
    title: {
        fontSize: 18,
        fontWeight: '600',
        color: '#111',
    },
    date: {
        fontSize: 14,
        color: '#666',
        marginTop: 4,
    },
    deleteButton: {
        backgroundColor: '#d93025',
        paddingHorizontal: 12,
        paddingVertical: 8,
        borderRadius: 6,
    },
    deleteButtonText: {
        color: '#fff',
        fontWeight: 'bold',
        fontSize: 14,
    },
    empty: {
        textAlign: 'center',
        marginTop: 30,
        color: '#666',
        fontSize: 16,
    },
});