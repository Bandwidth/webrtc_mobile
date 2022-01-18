# Bandwidth Android SDK

The Bandwidth Android SDK makes it quick and easy to build an excellent audio and video experience in your Android app. We provide tools to unlock the power of Bandwidth's audio and video networks.

## Installation

### Requirements

* Android

## Getting Started

```java
class MainActivity extends AppCompatActivity {
    private RTCBandwidth bandwidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Request camera permissions to allow capturing local video.

        // Setup any local and remote renderers.

        bandwidth = new RTCBandwidthClient(getApplicationContext(), EglBase.create().getEglBaseContext());

        // Called when a remote stream has become available.
        bandwidth.setOnStreamAvailableListener((streamId, mediaTypes, audioTracks, videoTracks, alias) -> {
            runOnUiThread(() -> {
                // Add remote renderers to available video tracks.
            });
        });

        // Called when a remote stream has become unavailable.
        bandwidth.setOnStreamUnavailableListener(streamId -> {
            runOnUiThread(() -> {
                // Clear remote renterers for the corresponding stream.
            });
        });

        // Connect to Bandwidth using the device token returned via Bandwidth's server-side WebRTC APIs.
        bandwidth.connect(deviceToken, () -> {
            // Once connected start publishing media.
            bandwidth.publish("android", (streamId, mediaTypes, audioSource, audioTrack, videoSource, videoTrack) -> {
                runOnUiThread(() -> {
                    // Start capturing local video using the video source.

                    // Add local renderer to the available video track.
                });
            });
        });
    }
}
```
