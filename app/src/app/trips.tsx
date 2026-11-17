import { useCallback, useEffect, useState } from "react";
import {
  ActivityIndicator,
  Pressable,
  RefreshControl,
  ScrollView,
  StyleSheet,
  View,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

import { ThemedText } from "@/components/themed-text";
import { ThemedView } from "@/components/themed-view";

// Works when Expo Web and Spring Boot are running
// on the same computer.
//
// Later, move this into an environment/config file.
const API_BASE_URL = "http://localhost:8080";

type Trip = {
  id: string;
  userId: string;
  name: string;
  startDate: string | null;
  endDate: string | null;
  createdAt: string | null;
};

export default function TripsScreen() {
  const [trips, setTrips] = useState<Trip[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchTrips = useCallback(async () => {
    try {
      setError(null);

      const response = await fetch(`${API_BASE_URL}/api/v1/trips`);

      if (!response.ok) {
        throw new Error(
          `Failed to load trips. Server returned ${response.status}.`,
        );
      }

      const data: Trip[] = await response.json();

      setTrips(data);
    } catch (err) {
      console.error("Failed to fetch trips:", err);

      setError(
        err instanceof Error
          ? err.message
          : "Something went wrong while loading trips.",
      );
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => {
    fetchTrips();
  }, [fetchTrips]);

  const handleRefresh = () => {
    setRefreshing(true);
    fetchTrips();
  };

  if (loading) {
    return (
      <ThemedView style={styles.container}>
        <SafeAreaView style={styles.centered}>
          <ActivityIndicator size="large" />

          <ThemedText style={styles.loadingText}>Loading trips...</ThemedText>
        </SafeAreaView>
      </ThemedView>
    );
  }

  return (
    <ThemedView style={styles.container}>
      <SafeAreaView style={styles.safeArea}>
        <View style={styles.header}>
          <View>
            <ThemedText type="title">My Trips</ThemedText>

            <ThemedText style={styles.subtitle}>
              Your upcoming and saved trips
            </ThemedText>
          </View>

          <Pressable style={styles.refreshButton} onPress={handleRefresh}>
            <ThemedText style={styles.refreshButtonText}>Refresh</ThemedText>
          </Pressable>
        </View>

        {error ? (
          <View style={styles.errorContainer}>
            <ThemedText style={styles.errorTitle}>
              Unable to load trips
            </ThemedText>

            <ThemedText>{error}</ThemedText>

            <Pressable style={styles.retryButton} onPress={fetchTrips}>
              <ThemedText style={styles.retryButtonText}>Try Again</ThemedText>
            </Pressable>
          </View>
        ) : (
          <ScrollView
            style={styles.tripList}
            contentContainerStyle={styles.tripListContent}
            refreshControl={
              <RefreshControl
                refreshing={refreshing}
                onRefresh={handleRefresh}
              />
            }
          >
            {trips.length === 0 ? (
              <View style={styles.emptyContainer}>
                <ThemedText type="subtitle">No trips yet</ThemedText>

                <ThemedText style={styles.emptyText}>
                  Create a trip and it will appear here.
                </ThemedText>
              </View>
            ) : (
              trips.map((trip) => <TripCard key={trip.id} trip={trip} />)
            )}
          </ScrollView>
        )}
      </SafeAreaView>
    </ThemedView>
  );
}

function TripCard({ trip }: { trip: Trip }) {
  return (
    <ThemedView type="backgroundElement" style={styles.tripCard}>
      <View style={styles.tripCardHeader}>
        <ThemedText type="subtitle">{trip.name}</ThemedText>
      </View>

      <View style={styles.tripDetails}>
        <View style={styles.detailRow}>
          <ThemedText style={styles.label}>Start</ThemedText>

          <ThemedText>{formatDate(trip.startDate)}</ThemedText>
        </View>

        <View style={styles.detailRow}>
          <ThemedText style={styles.label}>End</ThemedText>

          <ThemedText>{formatDate(trip.endDate)}</ThemedText>
        </View>
      </View>
    </ThemedView>
  );
}

function formatDate(date: string | null) {
  if (!date) {
    return "Not set";
  }

  const parsedDate = new Date(`${date}T00:00:00`);

  return parsedDate.toLocaleDateString(undefined, {
    month: "short",
    day: "numeric",
    year: "numeric",
  });
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },

  safeArea: {
    flex: 1,
    width: "100%",
    maxWidth: 900,
    alignSelf: "center",
    paddingHorizontal: 24,
  },

  centered: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
  },

  loadingText: {
    marginTop: 16,
  },

  header: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    paddingTop: 24,
    paddingBottom: 24,
    gap: 16,
  },

  subtitle: {
    marginTop: 6,
    opacity: 0.65,
  },

  refreshButton: {
    backgroundColor: "#2196F3",
    paddingHorizontal: 18,
    paddingVertical: 10,
    borderRadius: 8,
  },

  refreshButtonText: {
    color: "#ffffff",
    fontWeight: "600",
  },

  tripList: {
    flex: 1,
  },

  tripListContent: {
    gap: 16,
    paddingBottom: 40,
  },

  tripCard: {
    padding: 20,
    borderRadius: 14,
  },

  tripCardHeader: {
    marginBottom: 16,
  },

  tripDetails: {
    gap: 10,
  },

  detailRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    gap: 20,
  },

  label: {
    opacity: 0.6,
    fontWeight: "600",
  },

  emptyContainer: {
    alignItems: "center",
    paddingVertical: 80,
  },

  emptyText: {
    marginTop: 8,
    opacity: 0.6,
  },

  errorContainer: {
    padding: 24,
    borderRadius: 14,
    gap: 12,
  },

  errorTitle: {
    fontWeight: "700",
  },

  retryButton: {
    backgroundColor: "#2196F3",
    alignSelf: "flex-start",
    paddingHorizontal: 18,
    paddingVertical: 10,
    borderRadius: 8,
    marginTop: 8,
  },

  retryButtonText: {
    color: "#ffffff",
    fontWeight: "600",
  },
});
