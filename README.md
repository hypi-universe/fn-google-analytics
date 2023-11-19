# Google Places

This function allows you to send events to the Google analytics API.

## Usage

You can use it by by adding a function to your Hypi `Query` or `Mutation` types under the schema.
If you don't have these types, add them, if you do, modify them. For example

```graphql
type Query {
    sendGAEvent(action: String = "send-event", events: [Json!]): Json @fn(name: "ga", version: "v4", src: "hypi", env: ["GA4_SECRET", "GA4_MEASUREMENT_ID"])
}
```

This example shows the required parameters for this function. Any of the arguments listed below can be freely added.
The name `sendGAEvent` is arbitrary, you can name it anything you like.
The return type is `Json` but you can create a custom type and return that instead. 
Note that the structure of the custom type must match the structure returned from this function.

Use Google's [official demo](https://ga-dev-tools.google/ga4/event-builder/?c=search) to build a GA4 event.

## Env keys

* `GA4_SECRET` env is required. You create an environment variable in your Hypi app with this name and provide the value on each instance that uses this function.
* `GA4_MEASUREMENT_ID` env is required IF you do not have a parameter called `measurement_id`.
* `action` - Currently the `send-event` action is the only one supported, it sends an event to GA4

## Arguments

All the arguments to the GA 4 API are [available here](https://developers.google.com/analytics/devguides/collection/protocol/ga4/reference?client_type=gtag#payload_post_body).

* `measurement_id: String` - Required if `GA4_MEASUREMENT_ID` is not provided, otherwise optional
* `client_id: String` Optional, if not provided, the function uses `hypi_fn_api_<account_id>`. Uniquely identifies a user instance of a web client. See send event to the Measurement Protocol.
* `user_id: String` Optional, if not provided, the function uses the account ID making the request
* `timestamp_micros: Long` Optional, if not provided, the function uses the current time
* `user_properties` The user properties for the measurement. See [User properties](https://developers.google.com/analytics/devguides/collection/protocol/ga4/user-properties) for more information.
* `non_personalized_ads: Boolean` Set to true to indicate these events should not be used for personalized ads.
* `events: [Json!]` where `Json` a GA4 [event](https://developers.google.com/analytics/devguides/collection/protocol/ga4/reference/events) object - An array of event items. Up to 25 events can be sent per request. [See the events](https://developers.google.com/analytics/devguides/collection/protocol/ga4/reference/events) reference for all valid events.
                    The `name` property is required for each event in the `events` array.

Some common example events are

* [search](https://developers.google.com/analytics/devguides/collection/protocol/ga4/reference/events#search).
* [view_item](https://developers.google.com/analytics/devguides/collection/protocol/ga4/reference/events#view_item)
* [login](https://developers.google.com/analytics/devguides/collection/protocol/ga4/reference/events#login)
* [signup](https://developers.google.com/analytics/devguides/collection/protocol/ga4/reference/events#sign_up)
* [purchase](https://developers.google.com/analytics/devguides/collection/protocol/ga4/reference/events#purchase)

# Build & Release

1. Make sure you've logged into the Hypi container register by running `docker login hcr.hypi.app -u hypi` and enter a token from your Hypi account as the password
2. Build the JAR and copy the dependencies `mvn clean package`
3. Build the docker image `docker build . -t hcr.hypi.app/ga:v4`
4. Deploy the function `docker push hcr.hypi.app/ga:v4`

`ga` is the function name and `v4` is the version. Both are important for using the function later as shown in the overview above.

As one command :

```shell 
mvn clean package && docker build . -t hcr.hypi.app/ga:v4 && docker push hcr.hypi.app/ga:v4
```
