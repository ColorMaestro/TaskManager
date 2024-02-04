package me.colormaestro.taskmanager.integrations;

import me.colormaestro.taskmanager.enums.TaskStatus;
import me.colormaestro.taskmanager.model.Task;
import org.bukkit.Location;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import java.util.Objects;

public class DynmapOperator implements DynmapIntegration {

    private final MarkerAPI markerAPI;
    private MarkerSet markerSet;

    public DynmapOperator(DynmapAPI dynmapAPI) {
        markerAPI = dynmapAPI.getMarkerAPI();
        markerSet = markerAPI.getMarkerSet("active_tasks");
        if (markerSet == null) {
            markerSet = markerAPI.createMarkerSet("active_tasks", "Active Tasks", null, true);
            markerSet.setHideByDefault(true);
        }
    }

    @Override
    public void addInProgressTask(Task task, Location location) {
        int taskID = task.getId();
        if (task.getId() == null) {
            throw new IllegalArgumentException("Task is missing id");
        } else if (task.getStatus() != TaskStatus.DOING) {
            throw new IllegalArgumentException("Task is not in in-progress state");
        }

        String key = String.valueOf(taskID);
        String label = "[" + taskID + "] " + task.getTitle();;
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        markerSet.createMarker(key, label, Objects.requireNonNull(location.getWorld()).getName(), x, y, z,
                markerAPI.getMarkerIcon("orangeflag"), true);
    }

    @Override
    public void updateTaskFinishedMarkerIcon(String key) {
        Marker marker = markerSet.findMarker(key);
        if (marker != null) {
            marker.setMarkerIcon(markerAPI.getMarkerIcon("greenflag"));
        }
    }

    @Override
    public void removeTaskMarker(String key) {
        Marker marker = markerSet.findMarker(key);
        if (marker != null) {
            marker.deleteMarker();
        }
    }
}
