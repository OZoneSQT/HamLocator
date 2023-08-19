# HamLocator
The Android application is intended to retrieve a location from the mobile phone's GPS.

The location is displayed in "Digital Decimal" (DD) format and in "Degrees, Minutes and Seconds" (DMS) format. In addition, the location translated and displayed as JID grid used by radio fixtures using the Maidenhead algorithm to calculate the grid location used to determine distance and as a world puzzle when detecting connections.

The button is used to send the current location to the user's email address. The e-mail address is obtained from the Google account, via Google login, and with the push of a button the location and time are sent to the registered e-mail address.

## Privacy
No user telemetry is collected in the application and the application is intended to function fully without advertisements.

## Future develupment
* Widgit for the home screen is in development, means to show the JID grid location to the user as a gimmick.
* Translation for multiply languages.

## Issues:
Atempt to include Widget, is halted, due to a change in premissions, that sets restrictions on accessing background location added inAndroid 10 (sdk 29) and forvard.
