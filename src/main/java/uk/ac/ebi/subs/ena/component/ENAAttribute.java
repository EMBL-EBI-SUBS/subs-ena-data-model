package uk.ac.ebi.subs.ena.component;

public class ENAComponent {
    String tag = null;
    String value = null;
    String units = null;

    public ENAComponent() {}

    public ENAComponent(String tag, String value) {
        this.tag = tag;
        this.value = value;
    }

    public ENAComponent(String tag, String value, String units) {
        this.tag = tag;
        this.value = value;
        this.units = units;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ENAComponent that = (ENAComponent) o;

        if (!tag.equals(that.tag)) return false;
        if (!value.equals(that.value)) return false;
        return units != null ? units.equals(that.units) : that.units == null;
    }

    @Override
    public int hashCode() {
        int result = tag.hashCode();
        result = 31 * result + value.hashCode();
        result = 31 * result + (units != null ? units.hashCode() : 0);
        return result;
    }
}
