<?php
	/* MYSQL 설정 */
	$connect = mysqli_connect("localhost", "root", "ckdduq2580!", "ProjectR"); /* Mysql 접속 설정 */
	
	/* Mysql 접속 실패 시 나오는 구문 */
	if(mysqli_connect_errno($connect)) { echo mysqli_connect_error(); }

	mysqli_query($connect, "SET NAMES utf-8"); /* 인코딩 설정 */

	/* Android -> PHP(POST방식) */
	$NFC = $_POST["nfc"];
	$Port = $_POST["port"];
	$Date = $_POST["date"];

	$Query = "UPDATE GameClient SET state='0' WHERE Port='$Port' AND Date='$Date' AND NFC='$NFC'"; /* DB Table을 조회하는 쿼리문 변수 생성 */

	mysqli_query($connect, $Query); /* Query문 실행 및 변수 저장 */

	mysqli_close($connect);
?>