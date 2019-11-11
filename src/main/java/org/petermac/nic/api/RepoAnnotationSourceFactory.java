package org.petermac.nic.api;

import org.petermac.nic.dataminer.domain.pathos.vcf.core.GenomeBuild;
import org.petermac.nic.dataminer.domain.pathos.vcf.dao.AnnotationSourceDao;
import org.petermac.nic.dataminer.domain.pathos.vcf.repo.AnnotationSourceRepository;
import org.petermac.nic.dataminer.domain.pathos.vcf.sources.AnnotationSource;
import org.petermac.nic.dataminer.domain.pathos.vcf.sources.AnnotationSourceDelegate;
import org.petermac.nic.dataminer.domain.pathos.vcf.sources.AnnotationSourceFactory;
import org.petermac.nic.dataminer.domain.pathos.vcf.sources.AnnotationURLProvider;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Nic on 21/11/2018.
 */
@Service
public class RepoAnnotationSourceFactory implements AnnotationSourceFactory
{
    private Logger log = org.slf4j.LoggerFactory.getLogger(getClass());

    private final AnnotationSourceRepository repo;
    private final Map<String, AnnotationURLProvider> map;
    private Set<AnnotationURLProvider> all;
    private final AnnotationURLProvider ALLProvider = AnnotationSource.ALL.getAnnotationURLProvider();

    public RepoAnnotationSourceFactory(@Autowired AnnotationSourceRepository repo)
    {
        this.repo = repo;
        map = new HashMap<>();
    }

    @PostConstruct
    private void post()
    {
        for (AnnotationSourceDao dao : repo.findAll())
            map.put(dao.getName(),
                    new AnnotationSourceDelegate(dao.getName(), dao.getUrl(), dao.getQuery(), dao.isCachingAllowed(), dao.getBuilds().toArray(new GenomeBuild[dao.getBuilds().size()])));

        all = Collections.unmodifiableSet(new HashSet<>(map.values())); //concrete: Excluding ALL.
        map.put(ALLProvider.getName(), ALLProvider); //Support the 'ALL' functionality
        log.info("REPO AnnotationSources = {}", map.keySet());
        for (AnnotationURLProvider annotationURLProvider : map.values())
        {
            System.err.println(annotationURLProvider.getName() + " isCachingAllowed=" + annotationURLProvider.isCachingAllowed());
        }
    }

    public Set<AnnotationURLProvider> getAnnotationSources()
    {
        return all;
    }

    public Set<AnnotationURLProvider> getAnnotationSources(Function<String, String> sourceMapper, String... strings) throws IllegalArgumentException
    {
        log.info("getAnnotationSources");
        final Set<AnnotationURLProvider> urlProviders = Arrays.stream(strings).map(sourceMapper)
                .filter(Objects::nonNull)
                .peek(s -> log.info("REPO [{}]", s))
                .map(map::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (urlProviders.contains(ALLProvider))
            return all;
        return urlProviders;
    }
}
