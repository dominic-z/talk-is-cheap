import React, { useEffect, useRef, useState } from "react";
import usePrevious from "./usePrevious";

interface Props {
    name: string;
    completed: boolean;
    id: string;
    toggleTaskCompleted: (id: string) => void;
    onDelete: (id: string) => void;
    editTask: (id: string, name: string) => void;
}

export default function Todo(props: Props) {

    const [isEditing, setEditing] = useState(false);

    const [newName, setNewName] = useState(props.name);

    function onSave(e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault();
        setEditing(false);
        props.editTask(props.id, newName);
    }
    



    const editFieldRef = useRef<HTMLInputElement>(null);
    const editButtonRef = useRef<HTMLButtonElement>(null);
    const wasEditing = usePrevious<boolean>(isEditing);
    console.log("previous", wasEditing)
    console.log("now", isEditing)


    const editingTemplate = (
        <form className="stack-small" onSubmit={onSave}>
            <div className="form-group">
                <label className="todo-label" htmlFor={props.id}>
                    New name for {props.name}
                </label>
                <input id={props.id} className="todo-text"
                    type="text"
                    onChange={(e) => setNewName(e.target.value)}
                    ref={editFieldRef}
                />
            </div>
            <div className="btn-group">
                <button type="button" className="btn todo-cancel" onClick={() => setEditing(false)}>
                    Cancel
                    <span className="visually-hidden">renaming {props.name}</span>
                </button>
                <button type="submit" className="btn btn__primary todo-edit" >
                    Save
                    <span className="visually-hidden">new name for {props.name}</span>
                </button>
            </div>
        </form>
    );

    const viewTemplate = (
        <div className="stack-small">
            <div className="c-cb">
                <input
                    id={props.id}
                    type="checkbox"
                    defaultChecked={props.completed}
                    onChange={() => props.toggleTaskCompleted(props.id)}
                />
                <label className="todo-label" htmlFor={props.id}>
                    {newName}
                </label>
            </div>
            <div className="btn-group">
                <button type="button" className="btn" onClick={() => setEditing(true)}
                    ref={editButtonRef}>
                    Edit <span className="visually-hidden">{newName}</span>
                </button>
                <button
                    type="button"
                    className="btn btn__danger"
                    onClick={() => props.onDelete(props.id)}>
                    Delete <span className="visually-hidden">{newName}</span>
                </button>
            </div>
        </div>
    );


    useEffect(() => {
        if (!wasEditing && isEditing) {
            editFieldRef.current?.focus();
        }
        if (wasEditing && !isEditing) {
            editButtonRef.current?.focus();
        }
    }, [wasEditing, isEditing]);

    
    return (
        <li className="todo">
            {isEditing ? editingTemplate : viewTemplate}
        </li>
    )

}