
package br.com.conductor.heimdall.core.enums;

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

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>TypeExecutionPoint</h1><br/>
 * 
 * Enum that hold the types of ExecutionPoint.
 *
 * @author Filipe Germano
 *
 */
@Getter
@AllArgsConstructor
public enum TypeExecutionPoint {

     FIRST("pre"), SECOND("post");
     
     String filterType;

}
