package com.sickmartian.quickreminderwidget;

import junit.framework.Assert;

import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.junit.Test;

public class UtilsTest {

    @Test
    public void saneNowPlusDurationForAlarms() {
        DateTimeZone.setDefault(DateTimeZone.forID("America/Los_Angeles"));
        DateTimeUtils.setCurrentMillisFixed(LocalDateTime.parse("2018-03-11T01:34:00")
                .toDateTime(DateTimeZone.getDefault()).getMillis());

        Assert.assertEquals(LocalDateTime.parse("2018-03-11T03:04:00"),
                Utils.saneNowPlusDurationForAlarms(Duration.standardMinutes(30)));

        DateTimeUtils.setCurrentMillisOffset(0L);
    }

    @Test
    public void convertLocalToDTSafely() {
        DateTimeZone.setDefault(DateTimeZone.forID("America/Los_Angeles"));

        Assert.assertEquals(LocalDateTime.parse("2018-03-11T03:04:00").toDateTime(DateTimeZone.getDefault()),
                Utils.convertLocalToDTSafely(LocalDateTime.parse("2018-03-11T02:04:00")));

        Assert.assertEquals(LocalDateTime.parse("2018-03-11T03:04:00").toDateTime(DateTimeZone.getDefault()),
                Utils.convertLocalToDTSafely(LocalDateTime.parse("2018-03-11T02:04:00")));

        Assert.assertEquals(LocalDateTime.parse("2018-03-11T03:04:00").toDateTime(DateTimeZone.getDefault()),
                Utils.convertLocalToDTSafely(LocalDateTime.parse("2018-03-11T02:34:00")));
    }

}
