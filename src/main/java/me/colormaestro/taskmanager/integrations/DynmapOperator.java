package me.colormaestro.taskmanager.integrations;

import me.colormaestro.taskmanager.enums.TaskStatus;
import me.colormaestro.taskmanager.model.Task;
import org.bukkit.Location;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.GenericMarker;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import java.util.List;
import java.util.Objects;

public class DynmapOperator implements DynmapIntegration {
    private MarkerSet activeTasks;
    private final MarkerIcon orangeFlag;
    private final MarkerIcon greenFlag;

    public DynmapOperator(DynmapAPI dynmapAPI) {
        MarkerAPI markerAPI = dynmapAPI.getMarkerAPI();
        orangeFlag = markerAPI.getMarkerIcon("orangeflag");
        greenFlag = markerAPI.getMarkerIcon("greenflag");
        activeTasks = markerAPI.getMarkerSet("active_tasks");
        if (activeTasks == null) {
            activeTasks = markerAPI.createMarkerSet("active_tasks", "Active Tasks", null, true);
            activeTasks.setHideByDefault(true);
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
        activeTasks.createMarker(key, label, Objects.requireNonNull(location.getWorld()).getName(), x, y, z,
                orangeFlag, true);
    }

    @Override
    public void updateTaskFinishedMarkerIcon(String key) {
        Marker marker = activeTasks.findMarker(key);
        if (marker != null) {
            marker.setMarkerIcon(greenFlag);
        }
    }

    @Override
    public void removeTaskMarker(String key) {
        Marker marker = activeTasks.findMarker(key);
        if (marker != null) {
            marker.deleteMarker();
        }
    }

    @Override
    public void overwriteActiveTasks(List<Task> tasks) {
        clearMarkersFromMarkerSet(activeTasks);
        for (Task task : tasks) {
            if (task.getStatus() != TaskStatus.DOING && task.getStatus() != TaskStatus.FINISHED) {
                continue;
            }

            int taskID = task.getId();
            String key = String.valueOf(taskID);
            String label = "[" + taskID + "] " + task.getTitle();
            double x = task.getX();
            double y = task.getY();
            double z = task.getZ();
            MarkerIcon icon = task.getStatus() == TaskStatus.DOING ? orangeFlag : greenFlag;
            activeTasks.createMarker(key, label, task.getWorldName(), x, y, z, icon, true);
        }
    }

    private void clearMarkersFromMarkerSet(MarkerSet markerSet) {
        markerSet.getMarkers().forEach(GenericMarker::deleteMarker);
    }
}
