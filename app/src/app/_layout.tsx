import { DarkTheme, Stack, ThemeProvider } from "expo-router";

import * as SplashScreen from "expo-splash-screen";

import { AnimatedSplashOverlay } from "@/components/animated-icon";

SplashScreen.preventAutoHideAsync();

export default function RootLayout() {
  return (
    <ThemeProvider value={DarkTheme}>
      <AnimatedSplashOverlay />

      <Stack>
        <Stack.Screen
          name="index"
          options={{
            title: "Home",
          }}
        />

        <Stack.Screen
          name="trips"
          options={{
            title: "Trips",
          }}
        />

        <Stack.Screen
          name="explore"
          options={{
            title: "Explore",
          }}
        />
      </Stack>
    </ThemeProvider>
  );
}
