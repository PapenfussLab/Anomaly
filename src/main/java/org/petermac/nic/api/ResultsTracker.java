package org.petermac.nic.api;

import org.petermac.nic.dataminer.proj.EqualityException;
import org.petermac.nic.dataminer.proj.Timeable;
import org.springframework.stereotype.Service;

import java.beans.Transient;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Nic on 15/08/2018.
 */
@Service
public class ResultsTracker<T extends Timeable>
{
    private List<T> runningList = new ArrayList<>();
    private List<T> completedList = new ArrayList<>();
    private String last = "<none>";


    public void started(T timeable)
    {
        last = timeable.getId();
        completedList.remove(timeable);
        runningList.add(timeable);
    }

    public void finished(T timeable)
    {
        runningList.remove(timeable);
        completedList.add(timeable);
    }

    public String getLast()
    {
        return last;
    }

    public List<T> getCompleted()
    {
        return completedList;
    }

    public List<T> getRunning()
    {
        return runningList;
    }

    /**
     * @param tag
     * @return the completed Results, or null if no such tag exists, running or completed.
     * @throws FileNotFoundException if the job is still Running.
     */
    public T getCompleted(String tag) throws FileNotFoundException//Result not found
    {
        EqualityException.assertNotEmpty("tag", tag);
        return _getCompleted(tag.trim());
    }

    private T _getCompleted(String tag) throws FileNotFoundException//Result not found
    {
        final T completed = completedList.stream().filter(t -> tag.equals(t.getId())).findFirst().orElse(null);
        if (completed != null)
            return completed;

        if (getRunningTags().contains(tag))
            throw new FileNotFoundException(tag); //still running - No Content (busy)
        return null; //triggers 404
    }

    public T getRunning(String tag)
    {
        return runningList.stream().filter(t -> tag.equals(t.getId())).findFirst().orElse(null);
    }

    @Transient
    public Set<String> getCompletedTags()
    {
        return completedList.stream().map(Timeable::getId).filter(Objects::nonNull).collect(Collectors.toSet());

    }

    @Transient
    public Set<String> getRunningTags()
    {
        return runningList.stream().map(Timeable::getId).filter(Objects::nonNull).collect(Collectors.toSet());
    }
}

