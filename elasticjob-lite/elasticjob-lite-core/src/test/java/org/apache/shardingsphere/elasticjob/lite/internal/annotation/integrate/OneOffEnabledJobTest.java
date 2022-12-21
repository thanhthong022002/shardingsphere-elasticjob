/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.elasticjob.lite.internal.annotation.integrate;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.concurrent.BlockUtils;
import org.apache.shardingsphere.elasticjob.infra.env.IpUtils;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.infra.yaml.YamlEngine;
import org.apache.shardingsphere.elasticjob.lite.fixture.job.AnnotationUnShardingJob;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.internal.server.ServerStatus;
import org.junit.Before;
import org.junit.Test;

public final class OneOffEnabledJobTest extends BaseAnnotationTest {
    
    public OneOffEnabledJobTest() {
        super(TestType.ONE_OFF, new AnnotationUnShardingJob());
    }
    
    @Before
    public void assertEnabledRegCenterInfo() {
        assertThat(JobRegistry.getInstance().getCurrentShardingTotalCount(getJobName()), is(1));
        assertThat(JobRegistry.getInstance().getJobInstance(getJobName()).getServerIp(), is(IpUtils.getIp()));
        JobConfiguration jobConfig = YamlEngine.unmarshal(getREGISTRY_CENTER().get("/" + getJobName() + "/config"), JobConfigurationPOJO.class).toJobConfiguration();
        assertThat(jobConfig.getShardingTotalCount(), is(1));
        assertNull(jobConfig.getCron());
        assertThat(getREGISTRY_CENTER().get("/" + getJobName() + "/servers/" + JobRegistry.getInstance().getJobInstance(getJobName()).getServerIp()), is(ServerStatus.ENABLED.name()));
        assertThat(getREGISTRY_CENTER().get("/" + getJobName() + "/leader/election/instance"), is(JobRegistry.getInstance().getJobInstance(getJobName()).getJobInstanceId()));
        assertTrue(getREGISTRY_CENTER().isExisted("/" + getJobName() + "/instances/" + JobRegistry.getInstance().getJobInstance(getJobName()).getJobInstanceId()));
        getREGISTRY_CENTER().remove("/" + getJobName() + "/leader/election");
        assertTrue(getLeaderService().isLeaderUntilBlock());
    }
    
    @Test
    public void assertJobInit() {
        while (!((AnnotationUnShardingJob) getElasticJob()).isCompleted()) {
            BlockUtils.waitingShortTime();
        }
        assertTrue(getREGISTRY_CENTER().isExisted("/" + getJobName() + "/sharding"));
    }
    
}
