package org.petermac.nic.api;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.beans.Transient;
import java.util.function.Function;

/**
 * Created by Nic on 15/08/2018.
 */
public class Payload<T, C> extends ResourceSupport
{
    private final T payload;
    private final Class<C> controller;

    public Payload(T payload, Class<C> controller)
    {
        this.payload = payload;
        this.controller = controller;
    }

    public T getPayload()
    {
        return payload;
    }

    private ResourceSupport _ResourceSupport()
    {
        return this;
    }

    public C methodOn()
    {
        return ControllerLinkBuilder.methodOn(controller);
    }

    public Payload<T, C> self(final HttpEntity<Payload<T, C>> httpEntity)
    {
        _ResourceSupport().add(ControllerLinkBuilder.linkTo(httpEntity).withSelfRel());
        return this;
    }


    public Payload<T, C> linkTo(Function<C, HttpEntity> f)
    {
        return linkTo(f, "");
    }

    public Payload<T, C> linkTo(Function<C, HttpEntity> function, String title)
    {
        return linkTo(function.apply(methodOn()), title);
    }


    public Payload<T, C> linkTo(final HttpEntity httpEntity, String title)
    {
        final Link link = ControllerLinkBuilder.linkTo(httpEntity).withRel(Link.REL_NEXT).withTitle(title);
        _ResourceSupport().add(link);
        return this;
    }

    @Transient
    public ResponseEntity<Payload<T, C>> returnResponseEntity()
    {
        return returnResponseEntity(HttpStatus.OK);
    }

    @Transient
    public ResponseEntity<Payload<T, C>> returnResponseEntity(final HttpStatus httpStatus)
    {
        return new ResponseEntity<>(this, httpStatus);
    }
}
