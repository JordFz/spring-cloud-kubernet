package mx.jfc.microservices.core.recommendation.services;

import com.mongodb.DuplicateKeyException;
import mx.jfc.microservices.core.api.core.recommendation.Recommendation;
import mx.jfc.microservices.core.api.core.recommendation.RecommendationService;
import mx.jfc.microservices.core.api.exceptions.InvalidInputException;
import mx.jfc.microservices.core.recommendation.persistence.RecommendationEntity;
import mx.jfc.microservices.core.recommendation.persistence.RecommendationRepository;
import mx.jfc.microservices.core.util.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class RecommendationServiceImpl implements RecommendationService {

    private static Logger LOG = LoggerFactory.getLogger(RecommendationServiceImpl.class);

    private final ServiceUtil serviceUtil;
    private final RecommendationRepository repository;
    private final RecommendationMapper mapper;

    @Autowired
    public RecommendationServiceImpl(ServiceUtil serviceUtil, RecommendationRepository repository, RecommendationMapper mapper){
        this.serviceUtil = serviceUtil;
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Recommendation createRecommendation(Recommendation body) {
        try{
            RecommendationEntity entity = mapper.apiToEntity(body);
            RecommendationEntity newentity = repository.save(entity);

            LOG.debug("createRecommendation: created a recommendation entity: {}/{}", body.getProductId(), body.getRecommendationId());
            return mapper.entityToApi(newentity);
        } catch(DuplicateKeyException dke){
            throw new InvalidInputException("Duplicate key, product id: "+ body.getProductId() + ", Recommendation id: "+ body.getRecommendationId());
        }
    }

    @Override
    public List<Recommendation> getRecommendations(int productId) {

        if(productId < 1) {
            throw new InvalidInputException("Invalid productID " + productId);
        }
        List<RecommendationEntity> entityList = repository.findByProductId(productId);
        List<Recommendation> list = mapper.entityListToApiList(entityList);
        list.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));

        LOG.debug("getRecommendations: response size {} ", list.size());

        return list;
    }

    @Override
    public void deleteRecommendations(int productId) {
        LOG.debug("deleteRecommendations: tries to delete recommendantion for the product with productId: {}", productId);
        repository.deleteAll(repository.findByProductId(productId));

    }
}
