package com.jug.datahandling;

public class ArgumentValidation {
    public static void channelNumberIsValid(IImageProvider imageProvider, int channelNumber) {
        if (channelNumber < 0) {
            throw new IllegalArgumentException(String.format("channelNumber must be non-negative (channelNumber=%d)", channelNumber));
        }
        int availableChannels = imageProvider.getRawChannelImgs().size();
        if (channelNumber >= availableChannels) {
            throw new IllegalArgumentException(String.format("channelNumber is too large (channelNumber=%d, availableChannels=%d)", channelNumber, availableChannels));
        }
    }
}
