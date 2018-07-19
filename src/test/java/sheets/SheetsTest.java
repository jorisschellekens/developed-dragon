package sheets;

import org.testng.annotations.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SheetsTest {

    @Test
    public void getTest() throws GeneralSecurityException, IOException {
        GoogleSheet sheet = new GoogleSheet("15sOxKy0PkJbHW-0slXyuxeHuCXybmAy9q8ZOqkJLuZs");

        List<List<Object>> l = new ArrayList<>();
        l.add(Arrays.asList(new Object[]{"1"}));

        int emptyRow = sheet.emptyRow();
        sheet.set("a" + emptyRow, l);
    }
}
