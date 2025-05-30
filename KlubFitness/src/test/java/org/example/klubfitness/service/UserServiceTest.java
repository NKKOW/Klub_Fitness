package org.example.klubfitness.service;

import org.example.klubfitness.entity.User;
import org.example.klubfitness.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repo;

    @InjectMocks
    private UserService service;

    private User u1, u2, updatedPayload;

    @BeforeEach
    void setUp() {
        u1 = new User();
        u1.setId(1L);
        u1.setUsername("alice");
        u1.setPassword("pass1");

        u2 = new User();
        u2.setId(2L);
        u2.setUsername("bob");
        u2.setPassword("pass2");

        updatedPayload = new User();
        updatedPayload.setUsername("alice2");
        updatedPayload.setPassword("newpass");
    }

    @Test
    void getAllUsers_returnsListFromRepo() {
        when(repo.findAll()).thenReturn(Arrays.asList(u1, u2));

        List<User> result = service.getAllUsers();

        assertEquals(2, result.size());
        assertTrue(result.contains(u1));
        assertTrue(result.contains(u2));
        verify(repo, times(1)).findAll();
    }

    @Test
    void createUser_savesAndReturns() {
        when(repo.save(u1)).thenReturn(u1);

        User result = service.createUser(u1);

        assertSame(u1, result);
        verify(repo).save(u1);
    }

    @Test
    void getUserById_existingId_returnsUser() {
        when(repo.findById(1L)).thenReturn(Optional.of(u1));

        User result = service.getUserById(1L);

        assertSame(u1, result);
        verify(repo).findById(1L);
    }

    @Test
    void getUserById_nonExisting_returnsNull() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        User result = service.getUserById(99L);

        assertNull(result);
        verify(repo).findById(99L);
    }

    @Test
    void updateUser_existingId_updatesFieldsAndReturns() {
        when(repo.findById(1L)).thenReturn(Optional.of(u1));
        when(repo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = service.updateUser(1L, updatedPayload);

        assertNotNull(result);
        assertEquals("alice2", result.getUsername());
        assertEquals("newpass", result.getPassword());
        // zabezpieczamy, że repo.save był wywołany na zaktualizowanym obiekcie
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(repo).save(captor.capture());

        User saved = captor.getValue();
        assertEquals("alice2", saved.getUsername());
        assertEquals("newpass", saved.getPassword());
    }

    @Test
    void updateUser_nonExisting_returnsNull() {
        when(repo.findById(5L)).thenReturn(Optional.empty());

        User result = service.updateUser(5L, updatedPayload);

        assertNull(result);
        verify(repo, never()).save(any());
    }

    @Test
    void deleteUser_existingId_deletesAndReturnsTrue() {
        when(repo.existsById(1L)).thenReturn(true);

        boolean result = service.deleteUser(1L);

        assertTrue(result);
        verify(repo).deleteById(1L);
    }

    @Test
    void deleteUser_nonExisting_returnsFalse() {
        when(repo.existsById(42L)).thenReturn(false);

        boolean result = service.deleteUser(42L);

        assertFalse(result);
        verify(repo, never()).deleteById(anyLong());
    }
}
