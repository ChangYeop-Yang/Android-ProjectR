<?php
	/* MYSQL ���� */
	$connect = mysqli_connect("localhost", "root", "ckdduq2580!", "ProjectR"); /* Mysql ���� ���� */
	
	/* Mysql ���� ���� �� ������ ���� */
	if(mysqli_connect_errno($connect)) { echo mysqli_connect_error(); }

	mysqli_query($connect, "SET NAMES utf-8"); /* ���ڵ� ���� */

	/* Android -> PHP(POST���) */
	$NFC = $_POST["nfc"];
	$Port = $_POST["port"];
	$Date = $_POST["date"];

	$Query = "UPDATE GameClient SET state='0' WHERE Port='$Port' AND Date='$Date' AND NFC='$NFC'"; /* DB Table�� ��ȸ�ϴ� ������ ���� ���� */

	mysqli_query($connect, $Query); /* Query�� ���� �� ���� ���� */

	mysqli_close($connect);
?>