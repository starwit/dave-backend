package de.muenchen.dave.domain.dtos.laden.drilldown;

// The movement key (von → nach), used as column header
public record FahrbeziehungKey(int von, int nach) {
    @Override
    public String toString() {
        return von + "→" + nach;
    }
}
