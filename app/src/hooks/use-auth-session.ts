import { useEffect, useState } from 'react';
import { supabase } from '@/lib/supabase';

export function useAuthSession() {
    const [isSignedIn, setIsSignedIn] = useState(false);
    const [isAuthReady, setIsAuthReady] = useState(false);

    useEffect(() => {
        const {
            data: { subscription },
        } = supabase.auth.onAuthStateChange((_event, session) => {
            setIsSignedIn(Boolean(session));
            setIsAuthReady(true);
        });

        return () => subscription.unsubscribe();
    }, []);

    return { isSignedIn, isAuthReady };
}