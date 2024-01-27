# Project Overview

## Purpose (project requirements)

1. Create a provider application which supplies the following:
    - Albums (title and artist) to other applications
    - Albums should be able to be requested from this application
    - The application should notify other applications when when albums have changed so that they
      can update their UI
2. Create an application that requests albums (title and artist) from the provider app
    - Albums should be displayed in a list
    - The user should have the ability to add/ remove/ modify albums in this list

## Caveats
At least one of the applications (provider or editor) must be coded in Java

## Notes (from exercise creator)
- Provide source code via zip or remote (ex: github)
- Task is created to be open ended to evaluate skills
  - Style and "extras" are free to the developer
- Judgement will be on the following:
  - Complete the requirements of the 2 applications
  - Thought process (comments/ docs?)
  - Code design
  - Familiarity with technology being used in the 2 applications
- When completed, include the following:
  - Source code
  - Build instructions (target, compile SDK, etc.)
- UI appearance of the applications do not need to be prioritized

## How to build the projects
### System requirements
- abc

### Provider app

```bash
gradle :kts_provider/assembleRelease
```

Output:

- [Folder](kts_provider/build/outputs/apk/release)
- [apk](kts_provider/build/outputs/apk/release/kts_provider-release-unsigned.apk)

### Editor app

```bash
gradle :kts_viewer/assembleRelease
```

Output:

- [Folder](kts_viewer/build/outputs/apk/release)
- [apk](kts_viewer/build/outputs/apk/release/kts_viewer-release-unsigned.apk)


