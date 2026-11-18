import React from 'react';
import { View, Text, StyleSheet } from 'react-native';

// "export default" tells Expo Router this is the main component for this page
export default function ActivitiesScreen() {
    return (
        <View style={styles.container}>
            <Text style={styles.text}>Hello World</Text>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: '#ffffff',
    },
    text: {
        fontSize: 24,
        fontWeight: 'bold',
        color: '#000000',
    },
});