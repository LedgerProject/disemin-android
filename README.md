# AgroXM Android app

AgroXM is an open IoT solution for smart agriculture, complemented by a data marketplace where farmers can share data in a fair manner.

## Overview

The AgroXM Android app serves as a farmer's log, for manually logging farm activities, as well as a full farm monitoring solution, where the farmer can view data from the weather stations and/or other sensors that are deployed in the field.

_The app is in early development stages. Please treat with extra care!_

## Building

To build the app from source, you need to pass the following environment variables, through `gradle` command line arguments, or by adding a `.env` properties file that will be automatically read into env variables by the build script:

- `GOOGLE_API_KEY` required for using Google Maps. You can get one following the guide [here](https://developers.google.com/maps/documentation/android-sdk/get-api-key).

- `BASE_URL` the url of the [disemin-middleware](https://github.com/LedgerProject/disemin-middleware) application, that hosts the AgroXM client API.

## Download

Early development builds can be found under the [releases section](https://github.com/LedgerProject/disemin-android/releases).

## License

```
Copyright 2020 EXM P.C.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
