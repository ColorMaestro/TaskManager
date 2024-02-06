package me.colormaestro.taskmanager.integrations;

import me.colormaestro.taskmanager.enums.TaskStatus;
import me.colormaestro.taskmanager.model.Task;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.GenericMarker;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import java.util.List;

public class DynmapOperator implements DynmapIntegration {
    private MarkerSet activeTasks;
    private MarkerSet idleTasks;
    private final MarkerIcon orangeFlag;
    private final MarkerIcon greenFlag;
    private final MarkerIcon exclamation;

    public DynmapOperator(DynmapAPI dynmapAPI) {
        MarkerAPI markerAPI = dynmapAPI.getMarkerAPI();
        orangeFlag = markerAPI.getMarkerIcon("orangeflag");
        greenFlag = markerAPI.getMarkerIcon("greenflag");
        exclamation = markerAPI.getMarkerIcon("exclamation");
        initMarkerSets(markerAPI);
    }

    private void initMarkerSets(MarkerAPI markerAPI) {
        activeTasks = markerAPI.getMarkerSet("active_tasks");
        if (activeTasks == null) {
            activeTasks = markerAPI.createMarkerSet("active_tasks", "Active Tasks", null, true);
            activeTasks.setHideByDefault(true);
        }
        idleTasks = markerAPI.getMarkerSet("idle_tasks");
        if (idleTasks == null) {
            idleTasks = markerAPI.createMarkerSet("idle_tasks", "Idle Tasks", null, true);
            idleTasks.setHideByDefault(true);
        }
    }

    @Override
    public void addInProgressTask(Task task) {
        validateInProgressTask(task);
        createActiveTaskMarker(task);
    }

    @Override
    public void markTaskAsFinished(String key) {
        Marker marker = activeTasks.findMarker(key);
        if (marker != null) {
            marker.setMarkerIcon(greenFlag);
        }
    }

    @Override
    public void removeActiveTask(String key) {
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

            createActiveTaskMarker(task);
        }
    }

    @Override
    public void overwriteIdleTasks(List<Task> tasks) {
        clearMarkersFromMarkerSet(idleTasks);
        for (Task task : tasks) {
            if (task.getStatus() != TaskStatus.DOING) {
                continue;
            }

            createTaskMarker(idleTasks, task, exclamation);
        }
    }

    private void validateInProgressTask(Task task) {
        if (task.getId() == null) {
            throw new IllegalArgumentException("Task is missing id");
        } else if (task.getStatus() != TaskStatus.DOING) {
            throw new IllegalArgumentException("Task is not in in-progress state");
        }
    }

    private void createActiveTaskMarker(Task task) {
        MarkerIcon icon = task.getStatus() == TaskStatus.DOING ? orangeFlag : greenFlag;
        createTaskMarker(activeTasks, task, icon);
    }

    private void createTaskMarker(MarkerSet markerSet, Task task, MarkerIcon icon) {
        int taskID = task.getId();
        String key = String.valueOf(taskID);
        String label = "[" + taskID + "] " + task.getTitle();
        markerSet.createMarker(key, label, task.getWorldName(), task.getX(), task.getY(), task.getZ(), icon, true);
    }

    private void clearMarkersFromMarkerSet(MarkerSet markerSet) {
        markerSet.getMarkers().forEach(GenericMarker::deleteMarker);
    }
}
