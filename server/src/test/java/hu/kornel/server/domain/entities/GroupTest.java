package hu.kornel.server.domain.entities;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GroupTest {

    @Test
    @DisplayName("addMember adds member to group")
    void addMember_addsSuccessfully() {
        Group group = Group.builder()
                .id(1L)
                .name("Test Group")
                .teacherId(10L)
                .createdAt(LocalDateTime.now())
                .active(true)
                .build();

        group.addMember(100L);

        assertThat(group.isMember(100L)).isTrue();
        assertThat(group.getMemberCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("removeMember removes member from group")
    void removeMember_removesSuccessfully() {
        Group group = Group.builder()
                .id(1L)
                .teacherId(10L)
                .active(true)
                .build();

        group.addMember(100L);
        group.addMember(200L);
        assertThat(group.getMemberCount()).isEqualTo(2);

        group.removeMember(100L);

        assertThat(group.isMember(100L)).isFalse();
        assertThat(group.getMemberCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("isOwnedBy returns true for correct teacher id")
    void isOwnedBy_returnsTrueForTeacher() {
        Group group = Group.builder()
                .id(1L)
                .teacherId(10L)
                .build();

        assertThat(group.isOwnedBy(10L)).isTrue();
        assertThat(group.isOwnedBy(20L)).isFalse();
    }

    @Test
    @DisplayName("activate and deactivate toggle the active flag")
    void activateAndDeactivate_toggleActiveStatus() {
        Group group = Group.builder()
                .id(1L)
                .active(false)
                .build();

        group.activate();
        assertThat(group.isActive()).isTrue();

        group.deactivate();
        assertThat(group.isActive()).isFalse();
    }

    @Test
    @DisplayName("getMemberCount reflects current size of members set")
    void getMemberCount_returnsAccurateCount() {
        Group group = Group.builder()
                .id(1L)
                .teacherId(10L)
                .build();

        group.addMember(101L);
        group.addMember(102L);
        group.addMember(103L);

        assertThat(group.getMemberCount()).isEqualTo(3);

        group.removeMember(101L);
        assertThat(group.getMemberCount()).isEqualTo(2);
    }
}
