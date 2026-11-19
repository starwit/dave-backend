package de.muenchen.dave.services;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import de.muenchen.dave.domain.dtos.ZaehlungByProjektNummerDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.Status;
import de.muenchen.dave.repositories.elasticsearch.ZaehlstelleIndex;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ZaehlstelleIndexServiceTest {

    private final ZaehlstelleIndex zaehlstelleIndex = Mockito.mock(ZaehlstelleIndex.class);

    private final ZaehlstelleIndexService service = new ZaehlstelleIndexService(
            null,
            null,
            null,
            null,
            zaehlstelleIndex,
            null,
            null);

    private static Zaehlung zaehlung(final String id, final String projektNummer, final String status) {
        final Zaehlung zaehlung = new Zaehlung();
        zaehlung.setId(id);
        zaehlung.setProjektNummer(projektNummer);
        zaehlung.setStatus(status);
        return zaehlung;
    }

    @Test
    public void testGetZaehlungenByProjektNummerPrefix() throws Exception {
        final Zaehlstelle zs = new Zaehlstelle();
        zs.setId("zs");
        // eine Zählstelle kann Zählungen mehrerer Systeme haben
        zs.setZaehlungen(new ArrayList<>(List.of(
                zaehlung("z1", "cronist-1", Status.CREATED.name()),
                zaehlung("z2", "other-1", Status.ACTIVE.name()),
                zaehlung("z3", null, Status.ACTIVE.name()))));
        Mockito.when(zaehlstelleIndex.findAllByZaehlungenProjektNummerStartsWith("cronist-")).thenReturn(List.of(zs));

        final List<ZaehlungByProjektNummerDTO> result = this.service.getZaehlungenByProjektNummerPrefix("cronist-");

        assertThat(result.size(), is(equalTo(1)));
        assertThat(result.get(0).getId(), is(equalTo("z1")));
        assertThat(result.get(0).getProjektNummer(), is(equalTo("cronist-1")));
        assertThat(result.get(0).getStatus(), is(equalTo(Status.CREATED.name())));
        assertThat(result.get(0).getZaehlstelleId(), is(equalTo("zs")));
    }

    @Test
    public void testGetZaehlungenByProjektNummerPrefixRejectsEmptyPrefix() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> this.service.getZaehlungenByProjektNummerPrefix(" "));
        Mockito.verifyNoInteractions(zaehlstelleIndex);
    }

    @Test
    public void testUpdateZaehlstelleWithZaehlung() {

        Zaehlstelle zs = new Zaehlstelle();
        zs.setZaehlungen(new ArrayList<>());

        Zaehlung z1 = new Zaehlung();
        z1.setDatum(LocalDate.of(2010, 1, 1));
        z1.setJahr("2010");
        z1.setStatus(Status.ACTIVE.name());

        Zaehlung z2 = new Zaehlung();
        z2.setDatum(LocalDate.of(2015, 5, 1));
        z2.setJahr("2015");
        z2.setStatus(Status.ACTIVE.name());

        Zaehlung z3 = new Zaehlung();
        z3.setDatum(LocalDate.of(2020, 7, 1));
        z3.setJahr("2020");
        z3.setStatus(Status.ACTIVE.name());

        // letzte hinzugefügte Zählung ist älter als die vorhande
        this.service.updateZaehlstelleWithZaehlung(zs, z2);
        this.service.updateZaehlstelleWithZaehlung(zs, z1);
        assertThat(zs.getLetzteZaehlungJahr(), is(equalTo(2015)));
        assertThat(zs.getLetzteZaehlungMonatNummer(), is(equalTo(5)));

        // letztes hinzugefügtes Zählung ist die Jüngste
        this.service.updateZaehlstelleWithZaehlung(zs, z3);
        assertThat(zs.getLetzteZaehlungJahr(), is(equalTo(2020)));
        assertThat(zs.getLetzteZaehlungMonatNummer(), is(equalTo(7)));

    }

}
