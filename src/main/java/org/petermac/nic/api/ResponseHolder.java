package org.petermac.nic.api;

import org.petermac.nic.dataminer.domain.pathos.vcf.core.MapSchema;
import org.petermac.nic.dataminer.proj.SystemInfo;
import org.petermac.nic.dataminer.proj.TM;
import org.petermac.nic.dataminer.proj.Timeable;
import org.slf4j.Logger;

import java.beans.Transient;
import java.util.Objects;

/**
 * Created by Nic on 7/12/2018.
 */
public class ResponseHolder implements Runnable, Timeable
{
    private Logger log = org.slf4j.LoggerFactory.getLogger(getClass());
    private final DomainModel domainModel;
    private TM t;
    //        private long started, finished;
    private String exception;
    private final Response response;
    private ResultsTracker<ResponseHolder> tracker;
    private final FauxFinal<String> tag;

    ResponseHolder(Response response, MapSchema derivedSchema)
    {
        this.response = response;
        this.domainModel = new DomainModel(derivedSchema);
        tag = new FauxFinal<>();
    }

    @Override
    public Timeable start()
    {
        t = new TM(tag.get());
        return this;
    }

    @Override
    @Transient
    public String getId()
    {
        return t == null ? null : t.getLabel();
    }


    @Override
    public String getTime()
    {
        return t == null ? "Not started" : t.toString();
    }

    @Override
    @Transient
    public long getStart()
    {
        return t == null ? 0 : t.getStart();
    }

    public String getStartTime()
    {
        return getStart() == 0 ? "never" : SystemInfo.formatDateTime(t.getStartDate());
    }

    @Override
    public void close() throws Exception
    {
        if (t != null)
        {
            t.latch();
            t.close();
        }
    }

    public void run()
    {
        try (final Timeable t = start())
        {
            if (tracker != null)
                tracker.started(this);
            response.setTime(this);

//            annotationResponseBuilder.runAnnotationsParallel(response);

            response.writeModels(domainModel); //parallel sources retrieved
            response.clearReturnResults();
        } catch (Exception e)
        {
            log.warn("", e);
            exception = e.toString();
            response.setExecutionException(e);
        } finally
        {
            if (tracker != null)
                tracker.finished(this);
        }
    }


    public void setTracker(String tag, ResultsTracker<ResponseHolder> tracker)
    {
        this.tracker = tracker;
        this.tag.set(tag).trim();//invariant
    }

//        public AnnotationController.ResultsTracker getTracker()
//        {
//            return tracker;
//        }

//        public String getFinished()
//        {
//            return finished == 0 ? "still running..." : new Date(finished).toString();
//        }
//
//        public String getElapsed()
//        {
//            return finished == 0 ? "still running..." : String.valueOf(finished - started);
//        }

    public String getException()
    {
        return exception;
    }

    public String getTag()
    {
        return tag.get();
    }

//        public Collection<String> getVariants() //duplicated in the responses
//        {
//            return response.getVariants();
//        }

    public long getOk()
    {
        return response.getOk();
    }

    public long getFailed()
    {
        return response.getFailed();
    }

    /**
     * Response holder only exposes summary,
     * A tag retrieval will return the response.
     *
     * @return
     */
    @Transient
    public Response getResponse()
    {
        return response;
    }


    public static class FauxFinal<T>
    {
        private T s;

        public T get()
        {
            return s;
        }

        public FauxFinal set(T s)
        {
            if (this.s != null)
                throw new IllegalStateException("Final String is already set to '" + this.s + "'. Cannot change to " + s);
            Objects.requireNonNull(s, "Final Object Cannot be null");
            this.s = s;
            return this;
        }

        public void trim()
        {
            if (s != null && s instanceof String)
                s = (T) ((String) s).trim();
        }

        @Override
        public String toString()
        {
            return s.toString();
        }
    }
}
