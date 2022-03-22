const changeIcon = document.querySelector('.loadmore');
changeIcon.addEventListener('click', function() {
  const x = document.querySelectorAll('.channel-img .item');
  for (i = 0; i < x.length; i++) {
    x[i].setAttribute("class", "item");
  }
});