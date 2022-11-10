package com.jug.datahandling;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ArgumentValidationTest {
    @Test
    public void channelNumberIsValid__for_channel_number_too_large__throws_IllegalArgumentException() {
        int channelNumber = 4;
        IImageProvider imageProvider = mock(IImageProvider.class);
        when(imageProvider.getNumberOfChannels()).thenReturn(3);
        Assert.assertThrows(IllegalArgumentException.class, () -> ArgumentValidation.channelNumberIsValid(imageProvider, channelNumber));
    }

    @Test
    public void channelNumberIsValid__for_negative_channel_number__throws_IllegalArgumentException() {
        int channelNumber = -1;
        IImageProvider imageProvider = mock(IImageProvider.class);
        when(imageProvider.getNumberOfChannels()).thenReturn(3);
        Assert.assertThrows(IllegalArgumentException.class, () -> ArgumentValidation.channelNumberIsValid(imageProvider, channelNumber));
    }
}
