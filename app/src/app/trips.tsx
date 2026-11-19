import { useCallback, useEffect, useState } from "react";
import {
  ActivityIndicator,
  Pressable,
  RefreshControl,
  ScrollView,
  StyleSheet,
  TextInput,
  View,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

import { ThemedText } from "@/components/themed-text";
import { ThemedView } from "@/components/themed-view";

const API_BASE_URL = "http://localhost:8080";

type Trip = {
  id: string;
  userId: string;
  name: string;
  startDate: string | null;
  endDate: string | null;
  createdAt: string | null;
};

type CreateTripRequest = {
  userId: string;
  name: string;
  startDate: string | null;
  endDate: string | null;
};

export default function TripsScreen() {
  const [trips, setTrips] = useState<Trip[]>([]);

  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [creating, setCreating] = useState(false);

  const [error, setError] = useState<string | null>(null);
  const [createError, setCreateError] = useState<string | null>(null);
  const [createMessage, setCreateMessage] = useState<string | null>(null);

  // ---------------------------------------------------------
  // Create Trip form state
  // ---------------------------------------------------------

  const [userId, setUserId] = useState("");
  const [name, setName] = useState("");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");

  // ---------------------------------------------------------
  // GET trips
  // ---------------------------------------------------------

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

  // ---------------------------------------------------------
  // POST trip
  // ---------------------------------------------------------

  const handleCreateTrip = async () => {
    setCreateError(null);
    setCreateMessage(null);

    if (!userId.trim()) {
      setCreateError("User ID is required.");
      return;
    }

    if (!name.trim()) {
      setCreateError("Trip name is required.");
      return;
    }

    const request: CreateTripRequest = {
      userId: userId.trim(),
      name: name.trim(),
      startDate: startDate.trim() || null,
      endDate: endDate.trim() || null,
    };

    try {
      setCreating(true);

      const response = await fetch(`${API_BASE_URL}/api/v1/trips`, {
        method: "POST",

        headers: {
          "Content-Type": "application/json",
        },

        body: JSON.stringify(request),
      });

      if (!response.ok) {
        throw new Error(
          `Failed to create trip. Server returned ${response.status}.`,
        );
      }

      const createdTrip: Trip = await response.json();

      setCreateMessage(`Created "${createdTrip.name}".`);

      // Clear the trip fields after creation.
      // Keep userId because the same user may create another trip.
      setName("");
      setStartDate("");
      setEndDate("");

      // Reload trips so the new trip appears immediately.
      await fetchTrips();
    } catch (err) {
      console.error("Failed to create trip:", err);

      setCreateError(
        err instanceof Error
          ? err.message
          : "Something went wrong while creating the trip.",
      );
    } finally {
      setCreating(false);
    }
  };

  // ---------------------------------------------------------
  // Initial loading screen
  // ---------------------------------------------------------

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
        <ScrollView
          style={styles.tripList}
          contentContainerStyle={styles.tripListContent}
          refreshControl={
            <RefreshControl refreshing={refreshing} onRefresh={handleRefresh} />
          }
        >
          {/* -------------------------------------------------
              HEADER
          -------------------------------------------------- */}

          <View style={styles.header}>
            <View>
              <ThemedText type="title">My Trips</ThemedText>

              <ThemedText style={styles.subtitle}>
                Your upcoming and saved trips
              </ThemedText>
            </View>

            <Pressable style={styles.refreshButton} onPress={handleRefresh}>
              <ThemedText style={styles.buttonText}>Refresh</ThemedText>
            </Pressable>
          </View>

          {/* -------------------------------------------------
              CREATE TRIP
          -------------------------------------------------- */}

          <ThemedView type="backgroundElement" style={styles.createContainer}>
            <ThemedText type="subtitle">Create a Trip</ThemedText>

            <ThemedText style={styles.formDescription}>
              Add a new trip to your account.
            </ThemedText>

            <View style={styles.formGroup}>
              <ThemedText style={styles.formLabel}>User ID</ThemedText>

              <TextInput
                style={styles.input}
                value={userId}
                onChangeText={setUserId}
                placeholder="UUID"
                placeholderTextColor="#888"
                autoCapitalize="none"
                autoCorrect={false}
              />
            </View>

            <View style={styles.formGroup}>
              <ThemedText style={styles.formLabel}>Trip Name</ThemedText>

              <TextInput
                style={styles.input}
                value={name}
                onChangeText={setName}
                placeholder="Paris Vacation"
                placeholderTextColor="#888"
              />
            </View>

            <View style={styles.dateRow}>
              <View style={styles.dateField}>
                <ThemedText style={styles.formLabel}>Start Date</ThemedText>

                <TextInput
                  style={styles.input}
                  value={startDate}
                  onChangeText={setStartDate}
                  placeholder="YYYY-MM-DD"
                  placeholderTextColor="#888"
                  autoCapitalize="none"
                />
              </View>

              <View style={styles.dateField}>
                <ThemedText style={styles.formLabel}>End Date</ThemedText>

                <TextInput
                  style={styles.input}
                  value={endDate}
                  onChangeText={setEndDate}
                  placeholder="YYYY-MM-DD"
                  placeholderTextColor="#888"
                  autoCapitalize="none"
                />
              </View>
            </View>

            {createError ? (
              <ThemedText style={styles.errorText}>{createError}</ThemedText>
            ) : null}

            {createMessage ? (
              <ThemedText style={styles.successText}>
                {createMessage}
              </ThemedText>
            ) : null}

            <Pressable
              style={[styles.createButton, creating && styles.disabledButton]}
              onPress={handleCreateTrip}
              disabled={creating}
            >
              {creating ? (
                <ActivityIndicator />
              ) : (
                <ThemedText style={styles.buttonText}>Create Trip</ThemedText>
              )}
            </Pressable>
          </ThemedView>

          {/* -------------------------------------------------
              GET ERROR
          -------------------------------------------------- */}

          {error ? (
            <View style={styles.errorContainer}>
              <ThemedText style={styles.errorTitle}>
                Unable to load trips
              </ThemedText>

              <ThemedText>{error}</ThemedText>

              <Pressable style={styles.retryButton} onPress={fetchTrips}>
                <ThemedText style={styles.buttonText}>Try Again</ThemedText>
              </Pressable>
            </View>
          ) : null}

          {/* -------------------------------------------------
              TRIP LIST
          -------------------------------------------------- */}

          {!error && trips.length === 0 ? (
            <View style={styles.emptyContainer}>
              <ThemedText type="subtitle">No trips yet</ThemedText>

              <ThemedText style={styles.emptyText}>
                Create a trip and it will appear here.
              </ThemedText>
            </View>
          ) : null}

          {!error &&
            trips.map((trip) => <TripCard key={trip.id} trip={trip} />)}
        </ScrollView>
      </SafeAreaView>
    </ThemedView>
  );
}

// =========================================================
// TRIP CARD
// =========================================================

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

// =========================================================
// DATE FORMATTING
// =========================================================

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

// =========================================================
// STYLES
// =========================================================

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

  buttonText: {
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

  // -------------------------------------------------------
  // CREATE FORM
  // -------------------------------------------------------

  createContainer: {
    padding: 20,
    borderRadius: 14,
    gap: 16,
  },

  formDescription: {
    opacity: 0.65,
  },

  formGroup: {
    gap: 6,
  },

  formLabel: {
    fontWeight: "600",
    opacity: 0.75,
  },

  input: {
    borderWidth: 1,
    borderColor: "#555",
    borderRadius: 8,
    paddingHorizontal: 12,
    paddingVertical: 12,
    color: "#ffffff",
    backgroundColor: "#181818",
  },

  dateRow: {
    flexDirection: "row",
    gap: 12,
  },

  dateField: {
    flex: 1,
    gap: 6,
  },

  createButton: {
    backgroundColor: "#2196F3",
    alignItems: "center",
    paddingHorizontal: 18,
    paddingVertical: 12,
    borderRadius: 8,
  },

  disabledButton: {
    opacity: 0.5,
  },

  errorText: {
    color: "#ff6b6b",
  },

  successText: {
    color: "#6fcf97",
  },

  // -------------------------------------------------------
  // TRIP CARDS
  // -------------------------------------------------------

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

  // -------------------------------------------------------
  // EMPTY / ERROR STATES
  // -------------------------------------------------------

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
});
