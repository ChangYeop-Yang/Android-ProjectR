<?php
	/* MYSQL ���� */
	$connect = mysqli_connect("localhost", "root", "ckdduq2580!", "ProjectR"); /* Mysql ���� ���� */

	mysqli_query("SET NAMES utf-8"); /* ���ڵ� ���� */
	
	$Port = $_GET["port"]; /* ���� */
	$Date = $_GET["date"]; /* ��¥ */
	$Name = $_GET["name"]; /* �� */

	$Query = "SELECT * FROM GameClient WHERE Port='$Port' AND Date='$Date' AND Name='$Name'"; /* DB Table�� ��ȸ�ϴ� ������ ���� ���� */

	$result = mysqli_query($connect, $Query); /* Query�� ���� �� ���� ���� */
	
	$total_number = mysqli_num_rows($result); /* ���� DB�� ����� �� ������ �� ��ȸ */

	/* XML ���� ������ִ� ���� */
	$xml_result = "<?xml version = '1.0' encoding = 'utf-8'?>"; /* XML ó�� �κ� ���� */
	$xml_result .= "<client>"; /* XML ���� �κ� */
	$xml_result .= "<total_rows>$total_number</total_rows>"; /* �� ���� */
	$xml_result .= "</client>"; /* XML �� �κ� */
	
	header('Content-type: text/xml'); /* XML Type ���� */
	echo $xml_result;

	mysqli_close($connect); /* Mysql ���� */
?>