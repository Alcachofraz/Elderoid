package com.alcachofra.elderoid.utils;

public class ContactInfo implements Comparable<ContactInfo> {
    private String name;
    private String telephone;

    /**
     * Compares this ContactInfo to another ContactInfo (sorting environment). Compares telephone.
     * @param c Another ContactInfo.
     * @return Same as String.compareTo().
     */
    @Override
    public int compareTo(ContactInfo c) {
        int temp = getName().compareTo(c.getName());
        if (temp == 0) return getTelephone().compareTo(c.getTelephone());
        return temp;
    }

    /**
     * Compares this ContactInfo to another ContactInfo (use to distinguish two ContactInfo). Compares name and telephone.
     * @param o Another ContactInfo.
     * @return True if the same.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof ContactInfo) {
            ContactInfo c = (ContactInfo) o;
            return getName().equals(c.getName()) && getTelephone().equals(c.getTelephone());
        }
        return false;
    }

    /**
     * Returns a hash code for this object.
     * @return int a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return getName().hashCode() + getTelephone().hashCode();
    }

    /**
     * String value of this ContactInfo.
     * @return String containing this ContactInfo's information.
     */
    @Override
    public String toString() {
        return getName() + " [" + getTelephone() + "]";
    }

    /**
     * Get name of contact.
     * @return String containing name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set name of contact.
     * @param name String containing name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get Telephone of contact.
     * @return String containing telephone.
     */
    public String getTelephone() {
        return telephone;
    }

    /**
     * Set Telephone of contact.
     * @param telephone String containing telephone.
     */
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
}
