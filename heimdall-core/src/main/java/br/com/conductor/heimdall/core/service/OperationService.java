
package br.com.conductor.heimdall.core.service;

/*-
 * =========================LICENSE_START==================================
 * heimdall-core
 * ========================================================================
 * Copyright (C) 2018 Conductor Tecnologia SA
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ==========================LICENSE_END===================================
 */

import static br.com.conductor.heimdall.core.exception.ExceptionMessage.GLOBAL_RESOURCE_NOT_FOUND;
import static br.com.conductor.heimdall.core.exception.ExceptionMessage.ONLY_ONE_OPERATION_PER_RESOURCE;
import static br.com.twsoftware.alfred.object.Objeto.isBlank;
import static br.com.twsoftware.alfred.object.Objeto.notBlank;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.conductor.heimdall.core.converter.GenericConverter;
import br.com.conductor.heimdall.core.dto.OperationDTO;
import br.com.conductor.heimdall.core.dto.PageDTO;
import br.com.conductor.heimdall.core.dto.PageableDTO;
import br.com.conductor.heimdall.core.dto.page.OperationPage;
import br.com.conductor.heimdall.core.entity.Operation;
import br.com.conductor.heimdall.core.entity.Resource;
import br.com.conductor.heimdall.core.exception.HeimdallException;
import br.com.conductor.heimdall.core.repository.OperationRepository;
import br.com.conductor.heimdall.core.repository.ResourceRepository;
import br.com.conductor.heimdall.core.service.amqp.AMQPCacheService;
import br.com.conductor.heimdall.core.service.amqp.AMQPRouteService;
import br.com.conductor.heimdall.core.util.ConstantsCache;
import br.com.conductor.heimdall.core.util.Pageable;

/**
 * <h1>Operation Service</h1>
 * 
 * This class provides methods to create, read, updade and delete a {@link Operation} resource.
 * 
 * @author Filipe Germano
 *
 */
@Service
public class OperationService {

     @Autowired
     private OperationRepository operationRepository;

     @Autowired
     private ResourceRepository resourceRepository;

     @Autowired
     private AMQPRouteService amqpRoute;

     @Autowired
     private AMQPCacheService amqpCacheService;

     /**
      * Finds a {@link Operation} by its Id, {@link Resource} Id and {@link Api} Id.
      * 
      * @param  apiId						- The {@link Api} Id
      * @param 	resourceId					- The {@link Resource} Id
      * @param 	operationId					- The {@link Operation} Id
      * @return								The {@link Operation} found
      * @throws NotFoundException			Resource not found
      */
     @Transactional(readOnly = true)
     public Operation find(Long apiId, Long resourceId, Long operationId) {
          
          Operation operation = operationRepository.findByResourceApiIdAndResourceIdAndId(apiId, resourceId, operationId);      
          HeimdallException.checkThrow(isBlank(operation), GLOBAL_RESOURCE_NOT_FOUND);
                              
          return operation;
     }
     
     /**
      * Generates a paged list of {@link Operation} from a request.
      * 
      * @param  apiId						- The {@link Api} Id
      * @param 	resourceId					- The {@link Resource} Id
      * @param 	operationDTO				- The {@link OperationDTO}
      * @param 	pageableDTO					- The {@link PageableDTO}
      * @return								The paged {@link Operation} list as a {@link OperationPage} object
      * @throws NotFoundException			Resource not found
      */
     @Transactional(readOnly = true)
     public OperationPage list(Long apiId, Long resourceId, OperationDTO operationDTO, PageableDTO pageableDTO) {

          Resource resource = resourceRepository.findByApiIdAndId(apiId, resourceId);
          HeimdallException.checkThrow(isBlank(resource), GLOBAL_RESOURCE_NOT_FOUND);

          Operation operation = GenericConverter.mapper(operationDTO, Operation.class);
          operation.setResource(resource);
          
          Example<Operation> example = Example.of(operation, ExampleMatcher.matching().withIgnorePaths("resource.api").withIgnoreCase().withStringMatcher(StringMatcher.CONTAINING));
          
          Pageable pageable = Pageable.setPageable(pageableDTO.getOffset(), pageableDTO.getLimit());
          Page<Operation> page = operationRepository.findAll(example, pageable);
          
          OperationPage operationPage = new OperationPage(PageDTO.build(page));
          
          return operationPage;
     }

     /**
      * Generates a list of {@link Operation} from a request.
      * 
      * @param  apiId						- The {@link Api} Id
      * @param 	resourceId					- The {@link Resource} Id
      * @param 	operationDTO				- The {@link OperationDTO}
      * @return								The list of {@link Operation}
      * @throws NotFoundException			Resource not found
      */
     @Transactional(readOnly = true)
     public List<Operation> list(Long apiId, Long resourceId, OperationDTO operationDTO) {
          
          Resource resource = resourceRepository.findByApiIdAndId(apiId, resourceId);
          HeimdallException.checkThrow(isBlank(resource), GLOBAL_RESOURCE_NOT_FOUND);
          
          Operation operation = GenericConverter.mapper(operationDTO, Operation.class);
          operation.setResource(resource);
          
          Example<Operation> example = Example.of(operation, ExampleMatcher.matching().withIgnorePaths("resource.api").withIgnoreCase().withStringMatcher(StringMatcher.CONTAINING));
          
          List<Operation> operations = operationRepository.findAll(example);
          
          return operations;
     }
     
     /**
      * Saves a {@link Operation} to the repository.
      * 
      * @param  apiId						- The {@link Api} Id
      * @param 	resourceId					- The {@link Resource} Id
      * @param 	operationDTO				- The {@link OperationDTO}
      * @return								The saved {@link Operation}
      * @throws NotFoundException			Resource not found
      * @throws	BadRequestException			Only one operation per resource
      */
     @Transactional
     public Operation save(Long apiId, Long resourceId, OperationDTO operationDTO) {

          Resource resource = resourceRepository.findByApiIdAndId(apiId, resourceId);
          HeimdallException.checkThrow(isBlank(resource), GLOBAL_RESOURCE_NOT_FOUND);
                    
          Operation resData = operationRepository.findByResourceIdAndMethodAndPath(resourceId, operationDTO.getMethod(), operationDTO.getPath());
          HeimdallException.checkThrow(notBlank(resData) && (resData.getResource().getId() == resource.getId()), ONLY_ONE_OPERATION_PER_RESOURCE);
          
          Operation operation = GenericConverter.mapper(operationDTO, Operation.class);
          operation.setResource(resource);
          
          operation = operationRepository.save(operation);
          
          amqpRoute.dispatchRoutes();
          
          return operation;
     }

     /**
      * Updates a {@link Operation} by its Id, {@link Api} Id, {@link Resource} Id and {@link OperationDTO}.
      * 
      * @param  apiId						- The {@link Api} Id
      * @param 	resourceId					- The {@link Resource} Id
      * @param 	operationId					- The {@link Operation} Id
      * @param 	operationDTO				- The {@link OperationDTO}
      * @return								The updated {@link Operation}
      * @throws NotFoundException			Resource not found
      * @throws BadRequestException			Only one operation per resource
      */
     @Transactional
     public Operation update(Long apiId, Long resourceId, Long operationId, OperationDTO operationDTO) {

          Operation operation = operationRepository.findByResourceApiIdAndResourceIdAndId(apiId, resourceId, operationId);
          HeimdallException.checkThrow(isBlank(operation), GLOBAL_RESOURCE_NOT_FOUND);
          
          Operation resData = operationRepository.findByMethodAndPath(operationDTO.getMethod(), operationDTO.getPath());
          HeimdallException.checkThrow(notBlank(resData) && (resData.getResource().getId() == operation.getResource().getId()) && (resData.getId() != operation.getId()), ONLY_ONE_OPERATION_PER_RESOURCE);
          
          operation = GenericConverter.mapper(operationDTO, operation);
          
          operation = operationRepository.save(operation);
          
          amqpRoute.dispatchRoutes();
          
          amqpCacheService.dispatchClean(ConstantsCache.OPERATION_ACTIVE_FROM_ENDPOINT, operation.getResource().getApi().getBasePath() + operation.getPath());
          
          return operation;
     }
     
     /**
      * Deletes a {@link Operation} by its Id, {@link Resource} Id and {@link Api} Id.
      * 
      * @param  apiId						- The {@link Api} Id
      * @param 	resourceId					- The {@link Resource} Id
      * @param 	operationId					- The {@link Operation} Id
      * @throws NotFoundException			Resource not found
      */
     @Transactional
     public void delete(Long apiId, Long resourceId, Long operationId) {

          Operation operation = operationRepository.findByResourceApiIdAndResourceIdAndId(apiId, resourceId, operationId);
          HeimdallException.checkThrow(isBlank(operation), GLOBAL_RESOURCE_NOT_FOUND);
          
          operationRepository.delete(operation.getId());
          amqpCacheService.dispatchClean(ConstantsCache.OPERATION_ACTIVE_FROM_ENDPOINT, operation.getResource().getApi().getBasePath() + operation.getPath());
          
          amqpRoute.dispatchRoutes();
     }

}
