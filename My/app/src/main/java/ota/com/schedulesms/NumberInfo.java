package ota.com.schedulesms;

public class NumberInfo {

    // id
    public String number;
    public String normalizedNumber;

    // info from various sources
    public boolean isHiddenNumber;
    public ContactItem contactItem;
    // computed rating
    // precomputed for convenience
    public boolean noNumber;
    public String name;

}
