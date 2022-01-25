import com.polaris.sort.Sort;
import com.polaris.sort.entity.Location;
import com.polaris.sort.entity.TrackResult;

import java.util.Arrays;
import java.util.List;

public class SortTest {
    public static void main(String[] args) {
        Sort sort = new Sort();
        int x = 10, y = 10;
        for (int i = 0; i < 100; i++) {
            List<TrackResult> update = sort.update(Arrays.asList(new Location(x + i, y + i, x + 10 + i, y + i + 10, 1)));
            System.out.println(update);
        }

    }
}
