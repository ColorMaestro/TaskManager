package me.colormaestro.taskmanager.model;

/**
 * Objects implementing this interface are capable of providing necessary
 * information about their state in a form of {@link String} object.
 */
public interface StringReporter {

    /**
     *
     * @return corresponding information in form of String object
     */
    String getReport();
}
