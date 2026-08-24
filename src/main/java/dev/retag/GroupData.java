package dev.retag;

/** A nametag group: applied to players holding its permission. */
public final class GroupData {
    public final String name;
    public String prefix;
    public String suffix;
    /** Higher weight sorts closer to the top of the tab list. */
    public int weight;
    public String permission;

    public GroupData(String name, String prefix, String suffix, int weight, String permission) {
        this.name = name;
        this.prefix = prefix == null ? "" : prefix;
        this.suffix = suffix == null ? "" : suffix;
        this.weight = weight;
        this.permission = permission == null || permission.isEmpty()
                ? "retag.group." + name.toLowerCase() : permission;
    }
}
