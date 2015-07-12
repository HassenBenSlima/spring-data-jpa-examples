package net.petrikainulainen.springdata.jpa.web;

import com.nitorcreations.junit.runners.NestedRunner;
import net.petrikainulainen.springdata.jpa.todo.TodoDTO;
import net.petrikainulainen.springdata.jpa.todo.TodoDTOBuilder;
import net.petrikainulainen.springdata.jpa.todo.TodoSearchService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Petri Kainulainen
 */
@RunWith(NestedRunner.class)
public class TodoSearchControllerTest {

    private static final String SEARCH_TERM = "itl";

    private MockMvc mockMvc;

    private TodoSearchService searchService;

    @Before
    public void setUp() {
        searchService = mock(TodoSearchService.class);

        mockMvc = MockMvcBuilders.standaloneSetup(new TodoSearchController(searchService))
                .setMessageConverters(WebTestConfig.jacksonDateTimeConverter())
                .build();
    }

    public class FindBySearchTerm {

        public class WhenNoTodoEntriesAreFound {

            @Before
            public void returnZeroTodoEntries() {
                given(searchService.findBySearchTerm(SEARCH_TERM)).willReturn(new ArrayList<>());
            }

            @Test
            public void returnHttpResponseStatusOk() throws Exception {
                mockMvc.perform(get("/api/todo/search")
                                .param(WebTestConstants.REQUEST_PARAM_SEARCH_TERM, SEARCH_TERM)
                )
                        .andExpect(status().isOk());
            }

            @Test
            public void shouldReturnEmptyListAsJson() throws Exception {
                mockMvc.perform(get("/api/todo/search")
                                .param(WebTestConstants.REQUEST_PARAM_SEARCH_TERM, SEARCH_TERM)
                )
                        .andExpect(content().contentType(WebTestConstants.APPLICATION_JSON_UTF8))
                        .andExpect(jsonPath("$", hasSize(0)));
            }
        }

        public class WhenOneTodoEntryIsFound {

            private final Long ID = 1L;
            private static final String CREATION_TIME = "2014-12-24T22:28:39+02:00";
            private static final String DESCRIPTION = "description";
            private final String MODIFICATION_TIME = "2014-12-24T14:28:39+02:00";
            private final String TITLE = "title";

            @Before
            public void returnOneTodoEntry() {
                TodoDTO found = new TodoDTOBuilder()
                        .id(ID)
                        .creationTime(CREATION_TIME)
                        .description(DESCRIPTION)
                        .modificationTime(MODIFICATION_TIME)
                        .title(TITLE)
                        .build();

                given(searchService.findBySearchTerm(SEARCH_TERM)).willReturn(Arrays.asList(found));
            }

            @Test
            public void returnHttpResponseStatusOk() throws Exception {
                mockMvc.perform(get("/api/todo/search")
                                .param(WebTestConstants.REQUEST_PARAM_SEARCH_TERM, SEARCH_TERM)
                )
                        .andExpect(status().isOk());
            }

            @Test
            public void shouldReturnOneTodoEntryAsJson() throws Exception {
                mockMvc.perform(get("/api/todo/search")
                                .param(WebTestConstants.REQUEST_PARAM_SEARCH_TERM, SEARCH_TERM)
                )
                        .andExpect(content().contentType(WebTestConstants.APPLICATION_JSON_UTF8))
                        .andExpect(jsonPath("$", hasSize(1)))
                        .andExpect(jsonPath("$[0].id", is(ID.intValue())))
                        .andExpect(jsonPath("$[0].creationTime", is(CREATION_TIME)))
                        .andExpect(jsonPath("$[0].description", is(DESCRIPTION)))
                        .andExpect(jsonPath("$[0].modificationTime", is(MODIFICATION_TIME)))
                        .andExpect(jsonPath("$[0].title", is(TITLE)));
            }
        }
    }
}
