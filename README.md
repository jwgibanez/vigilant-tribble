# NRIC Barcode Scanner App

Maintainer: Juan Wilfredo Gales Ibañez

#### Functionality

The user should be able to scan the barcode found at the back of an NRIC or Workpass using the phone’s rear camera:
- The app ignores other kinds of cards, it should only scan NRICs and Workpasses
- The app displays the 9-character ID code extracted from the barcode
- The app captures a photo of the back of an NRIC or Workpass when
the barcode is scanned
- TODO: The photo should be cropped to show an image of the card only and not its
surroundings
- The photo is always at the correct orientation
- The app is able to tell if the 9-character ID code is valid or not
- The app is able to estimate the age of the owner of the NRIC or Workpass
- The user is able to edit the 9-character ID code but the application should only
accept valid id codes

## Usage

1. When the app is opened, user clicks button to start process of scanning NRIC barcode.
2. A camera finder view is shown for scanning, showing the back camera's view.
3. User scans the NRIC barcode with the back camera.
4. The app continuously analyses prence of barcode in the view.
5. If a valid NRIC barcode is detected, user is taken to a detail's page.
6. In the details page, the user will see the screenshot of the card, the NRIC number and estimate of age.
7. The user can change the NRIC value in the input field; the field will validate the format of the newly input number, and will re-estimate the age.

## Implementation

- Written in Kotlin
- Compatibility with API level 28 (Android Pie)
- One activity with three fragments: main, barcode scanner, details fragment
- Fragment navigation via Navigation Controller
- Implemented MVVM architecture via View Binding, ViewModels & LiveData
- Reactive programming via RxJava
- Android ML Kit for detecting barcode data

## Building locally

Clone the repository via
```
$ git clone https://github.com/jwgibanez/vigilant-tribble.git
```

Build the release apk
```
$ ./gradlew assembleRelease
```

## Testing

The architecture is consiously designed so that all layers are testable as much as possible.

Automated test have been implemented covering all layers in the MVVM architecture:
- View: UITest (MainActivity)
- ViewModel: BarcodeScanViewModelTest (BarcodeScanViewModel)

To run all test, attach device to adb and run
```
$ ./gradlew connectedAndroidTest
```

## CI/CD

On every commit to `main`, automated build is done by Github Actions to check the app is building successfully.
