import React, { useEffect, useState } from "react";
import { Routes, Route, Link, useNavigate } from "react-router-dom";
import axios from "axios";
import Modal from "./modiary.js";
import Home from "./Home.js";
import History from "./history.js";
import Pagination from "react-js-pagination";
import "./App.css";

function Notice() {
  let [text, setText] = useState("");
  const [length, setLength] = useState(0);
  let onChange = (event) => {
    const value = event.target.value;
    setText(value);
    setLength(value.length);
  };
  let [oldText, setOldText] = useState("");

  const [isModalOpen, setModalOpen] = useState(false);

  const openModal = (event) => {
    event.preventDefault();
    setModalOpen(true);
  };

  const closeModal = () => {
    setModalOpen(false);
  };

  let [userInformation, setUserInformation] = useState([]);
  let [email, setEmail] = useState("");
  let [jwtToken, setjwtToken] = useState();

  useEffect(() => {
    const userInfoFromStorage = localStorage.getItem("userInfo");
    if (userInfoFromStorage) {
      const userInfo = JSON.parse(userInfoFromStorage);
      setUserInformation(userInfo);
      setEmail(userInfo.email);
      setjwtToken(userInfo.jwtToken);
    }
  }, []);

  const navigate = useNavigate();

  const [page, setPage] = useState(1);
  const [items, setItems] = useState(1);
  const handlePageChange = (page) => {
    setPage(page);
  };
  const itemChange = (e) => {
    setItems(Number(e.target.value));
  };

  const sendDataToServer = async (data) => {
    try {
      const response = await api.post("/posts", data);
      console.log("성공:", response.data);
    } catch (error) {
      console.error("실패:", error);
    }
  };

  const sendUpdateToServer = async (id, data) => {
    try {
      const response = await api.put(`/posts/${id}`, data);
      console.log("수정 성공:", response.data);
      setUserInformation(
        userInformation.map((item) =>
          item.id === id ? { ...item, ...data } : item
        )
      );
    } catch (error) {
      console.error("수정 실패:", error);
    }
  };

  const sendDeleteToServer = async (id) => {
    try {
      const response = await api.delete(`/posts/${id}`);
      console.log("삭제 성공:", response.data);
      setUserInformation(userInformation.filter((item) => item.id !== id));
    } catch (error) {
      console.error("삭제 실패:", error);
    }
  };

  const loadDataToTextarea = (id, content) => {
    setText(content);
    setLength(content.length);
    setEditId(id);
  };

  const [modifyAndDelete, setModifyAndDelete] = useState(true);
  const [editId, setEditId] = useState(null);

  return (
    <div>
      <div>
        <h5>
          {email}님,
          <br />
          환영합니다.
          <br />
          <br />
          <div className="one"></div>
        </h5>
        <ul className="navigation-menu">
          <li>
            <Link to="/Home">홈</Link>
          </li>
          <br />
          <li>
            <div className="click">게시판</div>
          </li>
          <br />
          <li>
            <Link to="/History">히스토리</Link>
          </li>
          <br />
        </ul>
        <Routes>
          <Route path="/Home" element={<Home />}></Route>
          <Route path="/History" element={<History />}></Route>
        </Routes>
        <Modal isOpen={isModalOpen} onClose={closeModal} />
      </div>
      <div className="input_data_list">
        <textarea
          className="input"
          placeholder="내용을 입력하세요"
          style={{ whiteSpace: "pre-wrap" }}
          onChange={onChange}
          value={text}
          maxLength={199}
        ></textarea>
        <div className="inputLength">{length}/200자</div>
        <div>
          <button
            className="backrock_button"
            style={{ whiteSpace: "pre-wrap" }}
          >
            <img
              src="backrock_button.png"
              alt="backrock button"
              onClick={() => {
                setOldText(text);
                alert(editId ? "수정되었습니다." : "등록되었습니다.");
                if (editId) {
                  sendUpdateToServer(editId, { content: text });
                  setEditId(null);
                } else {
                  sendDataToServer({ content: text });
                }
                setText("");
                setLength(0);
              }}
            />
          </button>
        </div>

        {userInformation
          .slice(items * (page - 1), items * (page - 1) + items)
          .map((a, i) => {
            const canModifyAndDelete = email === a.email; // 현재 사용자가 작성한 글인지 확인
            return (
              <div key={i}>
                <div className="line1" />
                <div className="userdata">
                  <div className="bar">
                    <div className="title">{a.email}</div>
                    <div className="writetime">Price:{a.price}</div>
                  </div>
                  {canModifyAndDelete && (
                    <div>
                      <button
                        className="delete"
                        onClick={() => sendDeleteToServer(a.id)}
                      >
                        <img src="deleteButton.png" alt="delete button" />
                      </button>
                      <button
                        className="modify"
                        onClick={() => loadDataToTextarea(a.id, a.content)}
                      >
                        <img src="modifyButton.png" alt="modify button" />
                      </button>
                    </div>
                  )}
                  <div className="noticeContent">{a.content}</div>
                  <br />
                  <br />
                  <br />
                </div>
              </div>
            );
          })}
        <>
          <Pagination
            className="pagination"
            activePage={page}
            itemsCountPerPage={items}
            totalItemsCount={userInformation.length}
            pageRangeDisplayed={5}
            onChange={handlePageChange}
          ></Pagination>
        </>
      </div>
    </div>
  );
}

export default Notice;
