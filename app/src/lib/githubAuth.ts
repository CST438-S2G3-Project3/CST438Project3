import 'react-native-url-polyfill/auto';

import { makeRedirectUri } from 'expo-auth-session';
import * as WebBrowser from 'expo-web-browser';
import { supabase } from '@/lib/supabase';

WebBrowser.maybeCompleteAuthSession();

const redirectTo = makeRedirectUri({
    scheme: 'cst438project3',
});

export async function signInWithGitHub() {
    const { data, error } = await supabase.auth.signInWithOAuth({
        provider: 'github',
        options: {
            redirectTo,
            skipBrowserRedirect: true,
        },
    });
    
    if (error) throw error;
    if (!data.url) throw new Error('Supabase did not return a sign-in URL.');

    const result = await WebBrowser.openAuthSessionAsync(data.url, redirectTo);

    if (result.type !== 'success') return null;

    const callbackUrl = new URL(result.url);
    const hashParams = new URLSearchParams(callbackUrl.hash.slice(1));
    const getParam = (name: string) => callbackUrl.searchParams.get(name) ?? hashParams.get(name);

    const authError = getParam('error_description') ?? getParam('error') ?? getParam('error_code');

    if (authError) throw new Error(authError);

    const code = getParam('code');
    if (code) {
        const { data: sessionData, error: sessionError } = await supabase.auth.exchangeCodeForSession(code);

        if (sessionError) throw sessionError;

        return sessionData.session;
    }

    const accessToken = getParam('access_token');
    const refreshToken = getParam('refresh_token');

    if (!accessToken || !refreshToken) {
        throw new Error('The GitHub callback did not include session tokens.');
    }

    const { data: sessionData, error: sessionError} = await supabase.auth.setSession({
        access_token: accessToken,
        refresh_token: refreshToken,
    });

    if (sessionError) throw sessionError;

    return sessionData.session;
}