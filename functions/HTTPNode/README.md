# HTTP Node

## What is this function?

This function makes HTTP requests from the Greengrass Core.  This can be used to access HTTP
resources on the Greengrass Core's local network by any client that has access to publish to
the `${AWS_IOT_THING_NAME}/http_node/request` topic.

This can also be used by other functions that need to do simple HTTP requests but don't want to
include or maintain HTTP related code.

## What does the output look like?

`${AWS_IOT_THING_NAME}` is the name of the thing associated with your Core.

When the function receives a message on the `${AWS_IOT_THING_NAME}/http_node/request` topic that looks like this:

```json
{
  "id": "1",
  "action": "get",
  "url": "https://www.amazon.com"
}
```

It will initiate a `GET` request to `https://www.amazon.com`.  It will then respond with the content
retrieved from that URL, wrapped in a JSON message, on the topic `${AWS_IOT_THING_NAME}/http_node/response/ID` where `ID`
is the `id` value included in the request.

The `id`, `action`, and `url` fields are required.  If a required field is missing the request is
ignored and no errors are reported.

`action` can only be `get` or `post`.
