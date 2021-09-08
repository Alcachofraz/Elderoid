package com.alcachofra.elderoid.utils;

import android.text.format.DateUtils;

import com.alcachofra.elderoid.Elderoid;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class CallInfo implements Comparable<CallInfo> {

    SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yy HH:mm", new Locale(Elderoid.getLanguage()));
    SimpleDateFormat formatterDate = new SimpleDateFormat("dd/MMM/yy", new Locale(Elderoid.getLanguage()));
    SimpleDateFormat formatterTime = new SimpleDateFormat("HH:mm", new Locale(Elderoid.getLanguage()));

    /**
     * Call types.
     */
    public enum CallType {
        OUTGOING,
        INCOMING,
        MISSED
    }

    private final Date date;
    private final String number;
    private final String name;
    private final String duration;
    private final CallType callType;

    /**
     * Constructor of CallInfo.
     * @param date Date of call.
     * @param number Phone number on the other side of the call.
     * @param name Name of the contact on the other side (null if not a contact).
     * @param duration Duration of the call.
     * @param callType Call Type of this call.
     */
    public CallInfo(Date date, String number, String name, String duration, CallType callType) {
        this.date = date;
        this.number = number;
        this.name = name;
        this.duration = DateUtils.formatElapsedTime(Long.parseLong(duration));
        this.callType = callType;
    }

    /**
     * String value of this CallInfo.
     * @return String containing this CallInfo's information.
     */
    @Override
    public String toString() {
        return "CallInfo{" +
                ", date=" + date +
                ", number='" + number + '\'' +
                ", name='" + name + '\'' +
                ", duration='" + duration + '\'' +
                '}';
    }

    /**
     * Compares this CallInfo to another CallInfo (sorting environment). Compares date.
     * @param o Another CallInfo.
     * @return Same as Date.compareTo().
     */
    @Override
    public int compareTo(CallInfo o) {
        return getRawDate().compareTo(o.getRawDate());
    }

    /**
     * Compares this CallInfo to another CallInfo (use to distinguish two CallInfo). Compares date, number and call type.
     * @param o Another CallInfo.
     * @return True if the same.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CallInfo)) return false;
        CallInfo c = (CallInfo) o;
        return getRawDate().equals(c.getRawDate()) && getNumber().equals(c.getNumber()) && getCallType().equals(c.getCallType());
    }

    /**
     * Returns a hash code for this object.
     * @return int a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(date, number, callType);
    }

    /**
     * Get Date object of call.
     * @return Date object.
     */
    public Date getRawDate() {
        return date;
    }

    /**
     * Get date in String format, using formatterDate.
     * @return String containing date.
     */
    public String getDate() {
        return formatterDate.format(date);
    }

    /**
     * Get time in String format, using formatterTime.
     * @return String containing time.
     */
    public String getTime() {
        return formatterTime.format(date);
    }

    /**
     * Get date and time in String format, using formatter.
     * @return String containing date and time.
     */
    public String getDateTime() {
        return formatter.format(date);
    }

    /**
     * Get phone number on the other side of this call.
     * @return
     */
    public String getNumber() {
        return number;
    }

    /**
     * Get Name of contact on the other side of this call (null if not a contact).
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Get Duration of this call.
     * @return String containing duration.
     */
    public String getDuration() {
        return duration;
    }

    /**
     * Get call type.
     * @return CallType constant.
     */
    public CallType getCallType() {
        return callType;
    }
}
