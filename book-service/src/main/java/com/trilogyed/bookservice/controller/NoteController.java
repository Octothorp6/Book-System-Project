package com.trilogyed.bookservice.controller;

import com.trilogyed.bookservice.model.Note;
import com.trilogyed.bookservice.service.NoteService;
import com.trilogyed.bookservice.util.messages.NoteListEntry;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/notes")
public class NoteController {
    public static final String EXCHANGE = "note-exchange";
    public static final String ROUTING_KEY = "note.list.add.note";

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private NoteService noteService;

    public NoteController(RabbitTemplate rabbitTemplate, NoteService noteService) {
        this.rabbitTemplate = rabbitTemplate;
        this.noteService = noteService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createNote(@RequestBody NoteListEntry noteListEntry) {
        // create message to send to email list creation queue
        System.out.println("Sending note...");
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, noteListEntry);
        System.out.println("Message Sent");
    }

    @PutMapping(value = "/{noteId}")
    @ResponseStatus(HttpStatus.OK)
    public void updateNote(@PathVariable int noteId, @RequestBody Note note) {
        NoteListEntry noteListEntry = new NoteListEntry(note.getBookId(), note.getNote());
        noteListEntry.setNoteId(noteId);
        System.out.println("Updating note...");
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, noteListEntry);
        System.out.println("Note updated");
    }

    //  SIMPLE NOTE ROUTES, WILL BE USED INTERNALLY TO PERFORM OPERATIONS THAT DO NOT NEED TO USE THE QUEUE
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Note getNoteById(@PathVariable int id) {
        return noteService.findNote(id);
    }

    @GetMapping("/book/{bookId}")
    @ResponseStatus(HttpStatus.OK)
    public List<Note> getNotesByBookId(@PathVariable int bookId) {
        return noteService.findNotesByBookId(bookId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Note> getAllNotes() {
        return noteService.findAllNotes();
    }

}