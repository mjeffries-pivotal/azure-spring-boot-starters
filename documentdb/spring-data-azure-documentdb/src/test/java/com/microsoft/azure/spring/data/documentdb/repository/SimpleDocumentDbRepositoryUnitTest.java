/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.data.documentdb.repository;

import com.microsoft.azure.spring.data.documentdb.core.DocumentDbOperations;
import com.microsoft.azure.spring.data.documentdb.repository.domain.Person;
import com.microsoft.azure.spring.data.documentdb.repository.support.DocumentDbEntityInformation;
import com.microsoft.azure.spring.data.documentdb.repository.support.SimpleDocumentDbRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SimpleDocumentDbRepositoryUnitTest {

    private static final Person TEST_PERSON = new Person("aaa", "firstname", "lastname");
    SimpleDocumentDbRepository<Person, String> repository;
    @Mock
    DocumentDbOperations dbOperations;
    @Mock
    DocumentDbEntityInformation<Person, String> entityInformation;

    @Before
    public void setUp() {
        when(entityInformation.getJavaType()).thenReturn(Person.class);
        when(entityInformation.getCollectionName()).thenReturn(Person.class.getSimpleName());
        when(dbOperations.findAll(Person.class.getSimpleName(), Person.class)).thenReturn(Arrays.asList(TEST_PERSON));

        repository = new SimpleDocumentDbRepository<Person, String>(entityInformation, dbOperations);
    }

    @Test
    public void testSave() {
        repository.save(TEST_PERSON);

        assertEquals(1, repository.findAll().size());
        assertEquals(TEST_PERSON.getFirstName(), repository.findAll().get(0).getFirstName());
    }

    @Test
    public void testFindOne() {
        when(dbOperations.findById(anyObject(), any())).thenReturn(TEST_PERSON);

        repository.save(TEST_PERSON);

        final Person result = repository.findOne(TEST_PERSON.getId());

        assertEquals(result.getId(), TEST_PERSON.getId());
        assertEquals(result.getFirstName(), TEST_PERSON.getFirstName());
        assertEquals(result.getLastName(), TEST_PERSON.getLastName());

    }
}
