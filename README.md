# AppVerifier

AppVerifier is an app signing certificate hash viewer and verifier.\
It enables you to easily verify that your apps are genuine with others!

AppVerifier takes the app's package name and signing certificates hash(es) and compares them to the ones you provided or the ones in the internal database to verify that your apps are genuine.\
You can simply share the verification info to others and receive verification info from them and
share the received verification info to AppVerifier and you will see the verification status.\
AppVerifier does the heavy lifting for you 💪

This is a fork of [soupslurpr's AppVerifier](https://github.com/soupslurpr/AppVerifier). It adds status filters to
the app list, shows each app's installation source, has a searchable list of all signing keys in the internal database
and adds more apps to it. It uses its own package name and signing key, so it installs alongside the original
AppVerifier instead of updating it.

## Download

AppVerifier is available through [Obtainium](https://obtainium.imranr.dev) and
[GitHub releases](https://github.com/nicop111/AppVerifier/releases). Obtainium is the recommended way to get
AppVerifier, since it notifies you about and installs new releases.

### Obtainium

Click on the badge below on your phone to add AppVerifier to [Obtainium](https://obtainium.imranr.dev), or add
`https://github.com/nicop111/AppVerifier` as an app in Obtainium manually.

<a href="https://apps.obtainium.imranr.dev/redirect?r=obtainium://add/https://github.com/nicop111/AppVerifier">
    <img alt="Get it on Obtainium" src="https://raw.githubusercontent.com/ImranR98/Obtainium/main/assets/graphics/badge_obtainium.png" height="60">
</a>

### GitHub releases

Download the APK from the [latest release](https://github.com/nicop111/AppVerifier/releases/latest) and install it.

## Verifying AppVerifier

The package name and SHA-256 hash of the signing certificate is below, so you can verify AppVerifier with
[`apksigner`](https://developer.android.com/studio/command-line/apksigner#usage-verify) using
`apksigner verify --print-certs AppVerifier-nicop111-X.apk` before installing it.

DO NOT use AppVerifier to verify itself!

dev.nicop111.appverifier\
49:CE:D3:2C:24:B3:DF:74:44:87:E0:36:72:1C:6A:78:3E:20:19:AD:C6:8C:73:1F:F1:C2:1B:19:8F:5A:60:15

It is encouraged to verify it's the same with other people as well for assurance.

## Credits

AppVerifier was created by [soupslurpr](https://github.com/soupslurpr) together with its

