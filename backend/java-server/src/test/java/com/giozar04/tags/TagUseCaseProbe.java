package com.giozar04.tags;

import java.util.List;

import com.giozar04.tags.application.ports.output.TagRepository;
import com.giozar04.tags.application.usecases.TagUseCase;
import com.giozar04.tags.domain.entities.Tag;

/** Comprueba el caso de uso sin JDBC ni sockets. */
public final class TagUseCaseProbe {
    private static final class Repository implements TagRepository {
        String operation;
        Tag tag;
        long id;

        @Override public Tag createTag(Tag value) { operation = "create"; tag = value; return value; }
        @Override public Tag getTagById(long value) { operation = "get"; id = value; return tag; }
        @Override public Tag updateTagById(long value, Tag replacement) {
            operation = "update"; id = value; tag = replacement; return replacement;
        }
        @Override public void deleteTagById(long value) { operation = "delete"; id = value; }
        @Override public List<Tag> getAllTags() { operation = "all"; return List.of(tag); }
        @Override public List<Tag> getTagsByUserId(long value) {
            operation = "byUser"; id = value; return List.of(tag);
        }
    }

    public static void main(String[] args) {
        Repository repository = new Repository();
        TagUseCase useCase = new TagUseCase(repository);
        Tag tag = new Tag();
        tag.setUserId(7);
        tag.setName("Comida");
        tag.setColor("#123456");
        if (useCase.createTag(tag) != tag || !repository.operation.equals("create"))
            throw new AssertionError("create");
        if (useCase.getTagById(3) != tag || repository.id != 3) throw new AssertionError("get");
        if (useCase.updateTagById(3, tag) != tag || !repository.operation.equals("update"))
            throw new AssertionError("update");
        useCase.deleteTagById(3);
        if (!repository.operation.equals("delete") || repository.id != 3) throw new AssertionError("delete");
        if (useCase.getAllTags().size() != 1 || !repository.operation.equals("all"))
            throw new AssertionError("all");
        if (useCase.getTagsByUserId(7).size() != 1 || repository.id != 7)
            throw new AssertionError("byUser");
        String lastOperation = repository.operation;
        tag.setName(" ");
        expectInvalid(() -> useCase.createTag(tag));
        if (!repository.operation.equals(lastOperation)) throw new AssertionError("Se escribió una etiqueta inválida");
        expectInvalid(() -> useCase.deleteTagById(0));
        if (!repository.operation.equals(lastOperation)) throw new AssertionError("Se borró con ID inválido");
    }

    private static void expectInvalid(Runnable work) {
        try {
            work.run();
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("Se aceptó una entrada inválida");
    }
}
