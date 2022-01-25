import com.polaris.sort.Sort;
import com.polaris.sort.entity.Location;
import com.polaris.sort.entity.TrackResult;

import java.util.ArrayList;
import java.util.List;

public class SortTest {
    public static void main(String[] args) {
        Sort sort = new Sort();
        int x = 10, y = 10;
        for (int i = 0; i < 100; i++) {
            List<Location> detections = new ArrayList<>();
            detections.add(new Location(x + i, y + i, x + 10 + i, y + i + 10, 1));
            List<TrackResult> update = sort.update(detections);
            System.out.println(update);
        }
    }
}
