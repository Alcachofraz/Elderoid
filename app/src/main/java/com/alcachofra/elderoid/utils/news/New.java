package com.alcachofra.elderoid.utils.news;

import java.time.LocalDateTime;

public class New implements Comparable<New> {
    private final String title;
    private final String body;
    private final String link;
    private final String media;
    private final String id;
    private final LocalDateTime date;

    /**
     * Constructor of a New
     * @param title Title of new.
     * @param body Body of new.
     * @param date Publish date of new.
     * @param link Link to online article.
     * @param media URL to descriptive image.
     * @param id Unique ID of new.
     */
    public New(String title, String body, String date, String link, String media, String id) {
        this.title = title;
        this.body = body;
        this.link = link;
        this.date = LocalDateTime.parse(date, NewsManager.NEWS_DATE_TIME_FORMAT);
        this.media = media;
        this.id = id;
    }

    /**
     * Compare this New to another New (Use to sort News). The publish dates are compared.
     * @param n Another new.
     * @return The comparator value, negative if older, positive if more recent.
     */
    @Override
    public int compareTo(New n) {
        return getDateTime().compareTo(n.getDateTime());
    }

    /**
     * Compare this New to another New (Use to distinguish two News). The Title is compared.
     * @param o Another New.
     * @return True if both have the same title.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof New) {
            New n = (New) o;
            return getTitle().equals(n.getTitle());
        }
        return false;
    }

    /**
     * Returns a hash code for this New.
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        return getTitle().hashCode();
    }

    /**
     * String value of this New [<Title> (<Publish date>)].
     * @return String vlaue.
     */
    @Override
    public String toString() {
        return getTitle() + " (" + getDateTime().toString() + ")";
    }

    /**
     * Get Unique Id.
     * @return int containing unique ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Get New title.
     * @return String containing title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get Body of New.
     * @return String containing full body of New.
     */
    public String getBody() {
        return body;
    }

    /**
     * Get online article link.
     * @return String containing link to full original online article.
     */
    public String getLink() {
        return link;
    }

    /**
     * Get Date and Time.
     * @return LocalDateTime containing date and time.
     */
    public LocalDateTime getDateTime() {
        return date;
    }

    /**
     * Get Date.
     * @return String containing date.
     */
    public String getDateString() {
        return date.format(NewsManager.NEWS_DATE_FORMAT);
    }

    /**
     * Get Time.
     * @return String containing time.
     */
    public String getTimeString() {
        return date.format(NewsManager.NEWS_TIME_FORMAT);
    }

    /**
     * Get Date and Time.
     * @return String containing date and time.
     */
    public String getDateTimeString() {
        return date.format(NewsManager.NEWS_DATE_TIME_FORMAT);
    }

    /**
     * Get media link.
     * @return String containing URL to image.
     */
    public String getMediaLink() {
        return media;
    }
}
