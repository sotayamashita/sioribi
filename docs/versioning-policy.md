# Versioning

This project follows a Google Maps inspired scheme that is easy to read and
monotonic for Play Store uploads.

## Overview

- versionName: `YY.MM.WW.<git short hash>`
- versionCode: `YYMMWWnn`
- Tag: `v<versionName>`

## Definitions

- `YY`: two-digit year (e.g., 2026 -> 26)
- `MM`: two-digit month (01-12)
- `WW`: two-digit week-of-month (01-05)
- `<git short hash>`: 7-character short commit hash (e.g., `a1b2c3d`)
- `nn`: two-digit sequence number for releases within the same `YY.MM.WW`

## Rules

1. `versionCode` must be strictly increasing for every Play upload.
2. `versionName` is for humans; it includes the short git hash to identify the
   exact commit.
3. `versionCode` is numeric only and encodes the release time period plus a
   weekly sequence.
4. Tags must match the version name with a `v` prefix.

## Examples

- First release of the first week in January 2026
  - versionName: `26.01.01.a1b2c3d`
  - versionCode: `26010100`
  - tag: `v26.01.01.a1b2c3d`

- Third release in the second week of March 2026
  - versionName: `26.03.02.f00ba42`
  - versionCode: `26030202`
  - tag: `v26.03.02.f00ba42`

## Week-of-month

Week-of-month uses the calendar week within the month and is formatted as
`01-05`. The pipeline determines the week based on the release date in UTC.

## Notes

- If multiple releases happen in the same week, increment `nn` by 1 each time.
- If the calculated `versionCode` would not be strictly increasing, increment
  `nn` until it is.
