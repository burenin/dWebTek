<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
			<div class="header">
				<div class="left">
					<h1>Wiki</h1>
				</div>
				<div class="right">
					<a href="/Wiki/Entry">List all pages</a>
					<form name="search" method="get" action="/Wiki/Entry" onsubmit="setAction(this)">
						<input type="text" name="pattern" value="Search..." onclick="if(this.value=='Search...')this.value='';" />
						<input class="submit" type="submit" value="Search" /><br />
						<input class="checkbox" type="checkbox" name="luck" /><label>I am feeling lucky</label>
					</form>
				</div>
				<div class="floatbreaker"></div>
			</div>